package com.iptv.smartplayer.data.remote.xtream

import com.iptv.smartplayer.core.network.XtreamServerProvider
import com.iptv.smartplayer.data.repository.XtreamCredentialsProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * يبني روابط التشغيل المباشرة حسب صيغ Xtream Codes القياسية الموثّقة:
 * - مباشر:   {server}/live/{user}/{pass}/{stream_id}.{ext}   (عادة .ts أو .m3u8)
 * - أفلام:   {server}/movie/{user}/{pass}/{stream_id}.{ext}
 * - مسلسلات: {server}/series/{user}/{pass}/{episode_id}.{ext}
 *
 * ملاحظة مهمة: هذا الملف موجود كأداة جاهزة، لكن استدعاءه من الشاشات (Home/Movies/Series/LiveTv)
 * عند الانتقال لـPlayerScreen لم يُربط بعد — الشاشات حالياً تمرر contentId فقط عبر Navigation،
 * وPlayerScreen يحتاج تعديلاً ليستدعي هذا البنّاء (مع معرفة containerExtension الصحيح لكل عنصر)
 * قبل استدعاء playUrl() الفعلي. راجع قسم "المتبقي" في README للتفاصيل.
 */
@Singleton
class XtreamUrlBuilder @Inject constructor(
    private val credentialsProvider: XtreamCredentialsProvider,
    private val serverProvider: XtreamServerProvider,
) {
    private fun baseUrl(): String {
        val server = serverProvider.currentServerUrl()
        return "${server.scheme}://${server.host}:${server.port}"
    }

    fun buildLiveUrl(streamId: Int, extension: String = "ts"): String {
        val creds = credentialsProvider.current()
        return "${baseUrl()}/live/${creds.username}/${creds.password}/$streamId.$extension"
    }

    fun buildMovieUrl(streamId: Int, extension: String): String {
        val creds = credentialsProvider.current()
        return "${baseUrl()}/movie/${creds.username}/${creds.password}/$streamId.$extension"
    }

    fun buildEpisodeUrl(episodeId: String, extension: String): String {
        val creds = credentialsProvider.current()
        return "${baseUrl()}/series/${creds.username}/${creds.password}/$episodeId.$extension"
    }
}
