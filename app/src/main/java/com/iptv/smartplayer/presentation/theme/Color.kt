package com.iptv.smartplayer.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * لوحة ألوان نظام التصميم — مطابقة لمواصفات "Design System" في برومبت المشروع.
 * جميع الشاشات (رئيسية، أفلام، مسلسلات، بث مباشر، إعدادات) يجب أن تستهلك هذه القيم فقط
 * ولا تُستخدم ألوان Hex مباشرة داخل أي Composable آخر.
 */
object IptvColors {
    // خلفيات
    val BackgroundPrimary = Color(0xFF0A0A0F)
    val SurfaceDefault = Color(0xFF16161D)
    val SurfaceElevated = Color(0xFF1F1F29)

    // ألوان التمييز (Accent) — الافتراضي، قابل للتخصيص من إعدادات المستخدم
    val AccentPrimaryDefault = Color(0xFF6C5CE7)
    val AccentGradientStart = Color(0xFF6C5CE7)
    val AccentGradientEnd = Color(0xFF00CEC9)

    // خيارات إضافية للمستخدم في شاشة "المظهر"
    val AccentOptions = listOf(
        Color(0xFF6C5CE7), // بنفسجي (افتراضي)
        Color(0xFF00CEC9), // فيروزي
        Color(0xFFFF6B6B), // أحمر مرجاني
        Color(0xFFFFA502), // برتقالي
        Color(0xFF2ECC71), // أخضر
        Color(0xFF54A0FF), // أزرق
    )

    // النصوص
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA0A0B2)
    val TextDisabled = Color(0xFF5A5A66)

    // الحالات
    val StateFocusGlow = Color(0xFF6C5CE7)
    val StateError = Color(0xFFFF5C5C)
    val StateSuccess = Color(0xFF2ECC71)
    val StateLiveBadge = Color(0xFFE84118)

    // الطبقات الشفافة (Overlays / Scrims) فوق الصور والخلفيات
    val ScrimStart = Color(0x00000000)
    val ScrimEnd = Color(0xE6000000) // ~90% أسود
}
