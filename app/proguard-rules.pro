# قواعد ProGuard/R8 الأساسية — تُستكمل عند اقتراب الإطلاق الفعلي (بند "الإطلاق والنشر")
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.iptv.smartplayer.data.remote.**.*Dto { *; }
-keep class com.iptv.smartplayer.data.local.entity.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
