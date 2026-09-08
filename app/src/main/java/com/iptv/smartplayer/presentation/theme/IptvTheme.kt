package com.iptv.smartplayer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** يحمل لون التمييز (Accent) المختار من المستخدم كي تصل كل الشاشات إليه دون تمرير يدوي */
val LocalAccentColor = staticCompositionLocalOf { IptvColors.AccentPrimaryDefault }
val LocalIptvTypography = staticCompositionLocalOf { TvTypography }
val LocalIsTvDevice = staticCompositionLocalOf { true }

/**
 * الثيم الموحّد للتطبيق.
 * @param isTv عندما تكون true تُستخدم مقاسات TvTypography وهوامش TV؛ وإلا مقاسات الموبايل.
 * @param accentColor لون التمييز القابل للتخصيص من شاشة "المظهر" في الإعدادات.
 */
@Composable
fun IptvSmartPlayerTheme(
    isTv: Boolean = true,
    accentColor: Color = IptvColors.AccentPrimaryDefault,
    content: @Composable () -> Unit,
) {
    // التطبيق داكن دائماً على TV؛ يُسمح بثيم فاتح على الموبايل مستقبلاً عبر isSystemInDarkTheme()
    val typography = if (isTv) TvTypography else MobileTypography

    val colorScheme = darkColorScheme(
        primary = accentColor,
        secondary = IptvColors.AccentGradientEnd,
        background = IptvColors.BackgroundPrimary,
        surface = IptvColors.SurfaceDefault,
        onBackground = IptvColors.TextPrimary,
        onSurface = IptvColors.TextPrimary,
        error = IptvColors.StateError,
    )

    CompositionLocalProvider(
        LocalAccentColor provides accentColor,
        LocalIptvTypography provides typography,
        LocalIsTvDevice provides isTv,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = toMaterial3Typography(typography),
            content = content,
        )
    }
}
