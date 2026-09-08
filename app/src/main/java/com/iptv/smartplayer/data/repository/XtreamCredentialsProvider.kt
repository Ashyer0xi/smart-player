package com.iptv.smartplayer.data.repository

import com.iptv.smartplayer.core.network.XtreamServerProvider
import com.iptv.smartplayer.data.local.datastore.AccountsPreferences
import javax.inject.Inject
import javax.inject.Singleton

data class XtreamCredentials(val username: String, val password: String)

/**
 * يوفّر بيانات دخول الحساب النشط للـ Repositories عبر قراءة متزامنة خفيفة من الملف المشفَّر
 * (AccountsPreferences) مع تخزين مؤقت في الذاكرة لتفادي فتح/فك تشفير الملف مع كل طلب شبكة.
 * refresh() يُستدعى بعد كل تسجيل دخول ناجح أو تبديل حساب لتحديث القيمة المخزَّنة.
 */
@Singleton
class XtreamCredentialsProvider @Inject constructor(
    private val accountsPreferences: AccountsPreferences,
    private val xtreamServerProvider: XtreamServerProvider,
) {
    @Volatile
    private var cached: XtreamCredentials = XtreamCredentials("", "")
    @Volatile
    private var hasLoadedOnce = false

    fun current(): XtreamCredentials {
        // تحميل كسول عند أول استخدام فعلي بدل حظر Thread الرئيسي أثناء إنشاء الـSingleton.
        // TODO(إنتاج): هذا لا يزال I/O متزامناً على أول استدعاء؛ الأفضل استدعاء refresh()
        // مرة واحدة صراحةً من Dispatchers.IO عند بدء التطبيق (مثلاً من StartDestinationViewModel)
        // قبل أي شاشة تحتاج بيانات Xtream، بدل الاعتماد على هذا المسار الكسول هنا.
        if (!hasLoadedOnce) refresh()
        return cached
    }

    /** يُعاد قراءة الحساب النشط من التخزين المشفَّر ويُحدَّث عنوان سيرفر Xtream تبعاً له */
    fun refresh() {
        hasLoadedOnce = true
        val activeId = accountsPreferences.getActiveAccountIdBlocking()
        val account = activeId?.let { accountsPreferences.getAccount(it) }
        if (account != null) {
            cached = XtreamCredentials(account.username, account.password)
            xtreamServerProvider.updateServer(account.serverUrl)
        }
    }
}
