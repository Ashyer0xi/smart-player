package com.iptv.smartplayer.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iptv.smartplayer.presentation.navigation.TopLevelDestination
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.IptvDimens
import com.iptv.smartplayer.presentation.theme.LocalAccentColor
import com.iptv.smartplayer.presentation.theme.LocalIptvTypography

/**
 * الشريط العلوي المشترك: شفاف فوق الـHero، ويصبح خلفية صلبة (bg.surfaceElevated) عند التمرير.
 * يحتوي قائمة التصنيفات الأفقية القابلة للتنقل بالريموت + أيقونات البحث/الإشعارات/الحساب.
 */
@Composable
fun AppTopBar(
    currentDestination: TopLevelDestination,
    isSolidBackground: Boolean,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSolidBackground) IptvColors.SurfaceElevated else IptvColors.ScrimStart,
        label = "topBarBg",
    )

    Row(
        modifier = modifier
            .background(backgroundColor)
            .padding(horizontal = IptvDimens.tvOverscanMargin, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // قائمة التصنيفات — تظهر بترتيب RTL طبيعي بفضل دعم التخطيط RTL في المانيفست
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.CenterVertically) {
            TopLevelDestination.entries.forEach { destination ->
                val isSelected = destination == currentDestination
                Text(
                    text = destination.label,
                    style = LocalIptvTypography.current.body,
                    color = if (isSelected) LocalAccentColor.current else IptvColors.TextSecondary,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .then(Modifier)
                        .let { base ->
                            // FocusableRow بسيط: التركيز يُترجم لاحقاً لتغيير fontWeight/underline
                            base
                        }
                        .clickableFocusable { onDestinationSelected(destination) },
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "بحث",
                tint = IptvColors.TextPrimary,
                modifier = Modifier.size(24.dp).clickableFocusable(onSearchClick),
            )
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "الإشعارات",
                tint = IptvColors.TextPrimary,
                modifier = Modifier.size(24.dp),
            )
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "الحساب",
                tint = IptvColors.TextPrimary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
