package com.iptv.smartplayer.core.player

import android.content.Context
import android.util.Log
import android.view.Surface
import `is`.xyz.mpv.MPVLib
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class MpvState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val positionMillis: Long = 0L,
    val durationMillis: Long = 0L,
    val errorMessage: String? = null,
    val eofReached: Boolean = false,
)

/**
 * غلاف حول libmpv (`is.xyz.mpv.MPVLib`) — يخفي تفاصيل JNI الخام عن بقية التطبيق
 * ويعرض واجهة StateFlow مشابهة تماماً لواجهة ExoPlayer المستخدمة في PlayerViewModel،
 * كي تتعامل الشاشة مع أي من المحركين بنفس الطريقة.
 *
 * ملاحظة تقنية مهمة: أسماء دوال MPVLib أدناه (create/init/attachSurface/observeProperty/
 * command/addObserver...) مطابقة للنمط الموثّق في مشروع mpv-android الأصلي ونسخته المنشورة
 * على Maven Central (dev.jdtech.mpv:libmpv). عند فتح المشروع في Android Studio بعد Sync،
 * استخدم "Go to Declaration" على MPVLib للتأكد من تطابق التوقيعات بالضبط مع نسخة AAR
 * المُنزَّلة فعلياً (قد تختلف تفاصيل صغيرة بين إصدارات المكتبة).
 */
@Singleton
class MpvController @Inject constructor() : MPVLib.EventObserver {

    private var isInitialized = false
    private val _state = MutableStateFlow(MpvState())
    val state: StateFlow<MpvState> = _state.asStateFlow()

    /** يُستدعى مرة واحدة عند إنشاء السطح (Surface) الخاص بالعرض */
    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MPVLib.create(context)

            // إعدادات الفيديو: gpu context خاص بأندرويد + فك تشفير عتادي بنسخ (أكثر توافقاً من full hwdec)
            MPVLib.setOptionString("vo", "gpu")
            MPVLib.setOptionString("gpu-context", "android")
            MPVLib.setOptionString("opengl-es", "yes")
            MPVLib.setOptionString("hwdec", "mediacodec-copy")

            // إعدادات صوت متوافقة مع أغلب أجهزة Android TV
            MPVLib.setOptionString("ao", "audiotrack,opensles")

            // تعطيل تحكم المدخلات الافتراضي لـmpv لأن واجهة Compose تتولى كل التحكم
            MPVLib.setOptionString("input-default-bindings", "no")
            MPVLib.setOptionString("input-vo-keyboard", "no")
            MPVLib.setOptionString("input-cursor", "no")

            // بروتوكول التحليل الاستباقي: إعادة محاولة تلقائية عند انقطاع الشبكة بدل الفشل الفوري
            MPVLib.setOptionString("network-timeout", "20")
            MPVLib.setOptionString("stream-lavf-o", "reconnect=1,reconnect_streamed=1,reconnect_delay_max=5")

            MPVLib.init()

            MPVLib.addObserver(this)
            MPVLib.observeProperty("time-pos", MPVLib.mpvFormat.MPV_FORMAT_INT64)
            MPVLib.observeProperty("duration", MPVLib.mpvFormat.MPV_FORMAT_INT64)
            MPVLib.observeProperty("pause", MPVLib.mpvFormat.MPV_FORMAT_FLAG)
            MPVLib.observeProperty("eof-reached", MPVLib.mpvFormat.MPV_FORMAT_FLAG)
            MPVLib.observeProperty("paused-for-cache", MPVLib.mpvFormat.MPV_FORMAT_FLAG)

            isInitialized = true
        } catch (e: Exception) {
            Log.e("MpvController", "فشل تهيئة libmpv", e)
            _state.value = _state.value.copy(errorMessage = "تعذّر تشغيل محرك mpv على هذا الجهاز")
        }
    }

    fun attachSurface(surface: Surface) {
        if (!isInitialized) return
        MPVLib.attachSurface(surface)
        MPVLib.setOptionString("force-window", "yes")
        MPVLib.setOptionString("vid", "auto")
    }

    fun detachSurface() {
        if (!isInitialized) return
        MPVLib.setOptionString("force-window", "no")
        MPVLib.setOptionString("vid", "no")
        MPVLib.detachSurface()
    }

    fun playUrl(url: String) {
        if (!isInitialized) return
        _state.value = _state.value.copy(errorMessage = null, eofReached = false, isBuffering = true)
        MPVLib.command(arrayOf("loadfile", url))
    }

    fun togglePlayPause() {
        if (!isInitialized) return
        // نمرر الحالة الحالية للانعكاس (isPlaying=true يعني نريد pause=true الآن)
        MPVLib.setPropertyBoolean("pause", _state.value.isPlaying)
    }

    fun seekBy(deltaSeconds: Int) {
        if (!isInitialized) return
        MPVLib.command(arrayOf("seek", deltaSeconds.toString(), "relative"))
    }

    fun retry() {
        if (!isInitialized) return
        MPVLib.command(arrayOf("playlist-play-index", "current"))
    }

    fun release() {
        if (!isInitialized) return
        try {
            MPVLib.removeObserver(this)
            MPVLib.destroy()
        } catch (e: Exception) {
            Log.e("MpvController", "خطأ عند تحرير libmpv", e)
        } finally {
            isInitialized = false
        }
    }

    // ==================== MPVLib.EventObserver ====================

    override fun eventProperty(property: String) {
        // خاصية بدون قيمة مصاحبة (نادر الاستخدام هنا)
    }

    override fun eventProperty(property: String, value: Long) {
        when (property) {
            "time-pos" -> _state.value = _state.value.copy(positionMillis = value * 1000)
            "duration" -> _state.value = _state.value.copy(durationMillis = value * 1000)
        }
    }

    override fun eventProperty(property: String, value: Boolean) {
        when (property) {
            "pause" -> _state.value = _state.value.copy(isPlaying = !value)
            "eof-reached" -> _state.value = _state.value.copy(eofReached = value)
            "paused-for-cache" -> _state.value = _state.value.copy(isBuffering = value)
        }
    }

    override fun eventProperty(property: String, value: String) {
        // خصائص نصية (غير مستخدمة حالياً)
    }

    override fun event(eventId: Int) {
        // أحداث عامة مثل MPV_EVENT_END_FILE — يمكن التوسّع لاحقاً لتفريق أسباب الانتهاء (خطأ/طبيعي)
    }
}
