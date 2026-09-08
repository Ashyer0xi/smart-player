package com.iptv.smartplayer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** نقطة انطلاق حقن الاعتماديات (Hilt) لكامل التطبيق */
@HiltAndroidApp
class IptvApplication : Application()
