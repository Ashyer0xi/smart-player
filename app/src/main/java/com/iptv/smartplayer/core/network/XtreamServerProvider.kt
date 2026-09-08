package com.iptv.smartplayer.core.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject
import javax.inject.Singleton

/**
 * يوفّر عنوان سيرفر Xtream الخاص بالحساب النشط حالياً (يدعم "تبديل الحسابات المتعددة"
 * المذكور في شاشة الإعدادات). القيمة تُحدَّث من AccountRepository عند تبديل الحساب.
 */
@Singleton
class XtreamServerProvider @Inject constructor() {
    @Volatile
    private var activeServerUrl: HttpUrl = "http://example.com:8080/".toHttpUrl()

    fun currentServerUrl(): HttpUrl = activeServerUrl

    fun updateServer(url: String) {
        activeServerUrl = url.toHttpUrl()
    }
}
