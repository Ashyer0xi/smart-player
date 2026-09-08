package com.iptv.smartplayer.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow
import androidx.compose.ui.unit.dp
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvDimens
import com.iptv.smartplayer.presentation.theme.LocalAccentColor

/**
 * FocusableCard — المكوّن الأساسي الذي تُبنى فوقه كل بطاقات المحتوى
 * (بوسترات الأفلام/المسلسلات، بطاقات القنوات، بطاقات الحلقات، بطاقات الإعدادات...).
 *
 * يوفّر سلوك تركيز موحّداً عبر التطبيق بالكامل حسب نظام التصميم:
 * - تكبير 1.1x عند التركيز (Focus scale) بمدة 150ms.
 * - توهج (Glow) بلون التمييز (Accent) حول الحدود عند التركيز.
 * - ظل مرتفع (Elevation) عند التركيز لإحساس بالعمق.
 *
 * لا تُبنى أي بطاقة جديدة في الشاشات دون المرور عبر هذا المكوّن، لضمان
 * اتساق سلوك الريموت (D-pad) في كل مكان بالتطبيق.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(IptvDimens.cardCornerRadius),
    content: @Composable (isFocused: Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val accent = LocalAccentColor.current

    val scale by animateFloatAsState(
        targetValue = if (isFocused) IptvDimens.focusScale else 1f,
        animationSpec = tween(durationMillis = IptvDimens.focusAnimationMillis),
        label = "focusScale",
    )
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 16.dp else 2.dp,
        animationSpec = tween(durationMillis = IptvDimens.focusAnimationMillis),
        label = "focusElevation",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = elevation, shape = shape, ambientColor = accent, spotColor = accent)
            .background(color = IptvColors.SurfaceDefault, shape = shape)
            .border(
                border = BorderStroke(
                    width = if (isFocused) IptvDimens.focusBorderWidth else 0.dp,
                    color = if (isFocused) accent else IptvColors.SurfaceDefault,
                ),
                shape = shape,
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .onFocusChanged { /* يُستخدم لاحقاً للتمرير التلقائي BringIntoViewRequester */ },
    ) {
        content(isFocused)
    }
}
