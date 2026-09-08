package com.iptv.smartplayer.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * نظام الطباعة — يفترض إضافة خط "Cairo" أو "IBM Plex Sans Arabic" إلى res/font
 * ثم استبدال FontFamily.Default أدناه بـ FontFamily(Font(R.font.cairo_regular), ...).
 * تم فصل مقاسي TV والموبايل لأن شاشات TV تحتاج نصوصاً أكبر بنسبة 20-30% لتُقرأ من بعيد.
 */
private val AppFontFamily = FontFamily.Default // TODO: استبدال بخط Cairo / IBM Plex Sans Arabic

data class IptvTypography(
    val display: TextStyle,
    val headline: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
)

val TvTypography = IptvTypography(
    display = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 52.sp),
    headline = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp),
    title = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp),
    body = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    caption = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 18.sp),
)

val MobileTypography = IptvTypography(
    display = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    headline = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    title = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    body = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    caption = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
)

/** جسر توافق مع Material3 Typography كي تعمل مكونات androidx.compose.material3 مباشرة */
fun toMaterial3Typography(t: IptvTypography): Typography = Typography(
    displayLarge = t.display,
    headlineMedium = t.headline,
    titleMedium = t.title,
    bodyMedium = t.body,
    labelSmall = t.caption,
)
