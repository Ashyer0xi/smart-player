package com.iptv.smartplayer.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvDimens
import com.iptv.smartplayer.presentation.theme.LocalAccentColor
import com.iptv.smartplayer.presentation.theme.LocalIptvTypography

/** شارة التقييم الموحّدة (⭐ + رقم) — تُستخدم في كل بطاقة وشاشة تفاصيل */
@Composable
fun RatingBadge(rating: Double, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
        Text(
            text = String.format("%.1f", rating),
            style = LocalIptvTypography.current.caption,
            color = IptvColors.TextPrimary,
        )
    }
}

/** عنوان قسم موحّد + "عرض الكل" — السهم يُعكس تلقائياً في RTL لأن التخطيط الأساس RTL */
@Composable
fun SectionHeaderRow(
    title: String,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = LocalIptvTypography.current.headline, color = IptvColors.TextPrimary)
        if (onSeeAllClick != null) {
            Text(
                text = "عرض الكل ←",
                style = LocalIptvTypography.current.body,
                color = LocalAccentColor.current,
                modifier = Modifier.padding(end = 4.dp),
            )
        }
    }
}

/** بديل تحميل نابض (Shimmer) يُستخدم بدل شاشات التحميل الفارغة في كل الصفوف/الشاشات */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(IptvDimens.cardCornerRadius),
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerAlpha",
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(IptvColors.SurfaceElevated.copy(alpha = alpha)),
    )
}

/** حالة فارغة موحّدة — "لا نتائج" / "لا مفضلة بعد" مع اقتراح إجراء */
@Composable
fun EmptyStateView(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.SearchOff,
                contentDescription = null,
                tint = IptvColors.TextSecondary,
                modifier = Modifier.size(56.dp),
            )
            Text(title, style = LocalIptvTypography.current.title, color = IptvColors.TextPrimary, textAlign = TextAlign.Center)
            Text(message, style = LocalIptvTypography.current.body, color = IptvColors.TextSecondary, textAlign = TextAlign.Center)
            if (actionLabel != null && onActionClick != null) {
                Button(onClick = onActionClick) { Text(actionLabel) }
            }
        }
    }
}

/** حالة خطأ موحّدة — فشل شبكة، مع زر إعادة المحاولة */
@Composable
fun ErrorStateView(
    message: String,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.WifiOff,
                contentDescription = null,
                tint = IptvColors.StateError,
                modifier = Modifier.size(56.dp),
            )
            Text("حدث خطأ", style = LocalIptvTypography.current.title, color = IptvColors.TextPrimary)
            Text(message, style = LocalIptvTypography.current.body, color = IptvColors.TextSecondary, textAlign = TextAlign.Center)
            Button(onClick = onRetryClick) { Text("إعادة المحاولة") }
        }
    }
}

/** تدرج Scrim فوق الصور لضمان وضوح النص — من شفاف إلى أسود 90% */
fun verticalScrimBrush(): Brush = Brush.verticalGradient(
    colors = listOf(IptvColors.ScrimStart, IptvColors.ScrimEnd),
)
