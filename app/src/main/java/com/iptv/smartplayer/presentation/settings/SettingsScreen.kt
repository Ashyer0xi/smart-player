package com.iptv.smartplayer.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.LocalAccentColor
import com.iptv.smartplayer.presentation.theme.LocalIptvTypography

enum class SettingsTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ACCOUNT("الحساب والاشتراك", Icons.Filled.AccountCircle),
    APPEARANCE("المظهر", Icons.Filled.Palette),
    PLAYBACK("التشغيل", Icons.Filled.PlayCircle),
    EPG("EPG والقنوات", Icons.Filled.Tv),
    CATALOGS("الكتالوجات المخصصة", Icons.Filled.VideoLibrary),
    STORAGE("التنزيلات والتخزين", Icons.Filled.Storage),
    NOTIFICATIONS("الإشعارات", Icons.Filled.Notifications),
    PRIVACY("الخصوصية والأمان", Icons.Filled.Lock),
    BACKUP("النسخ الاحتياطي", Icons.Filled.Backup),
    ABOUT("حول التطبيق", Icons.Filled.Info),
}

/**
 * شاشة الإعدادات بتصميم Master-Detail بلوحين: قائمة تبويبات ثابتة + لوحة تفاصيل تتحدّث فورياً
 * دون الانتقال لشاشة جديدة — مثالي لتجربة الريموت (لا حاجة لضغط "رجوع" متكرر).
 */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableStateOf(SettingsTab.ACCOUNT) }
    val accentColor by viewModel.accentColor.collectAsState()
    val playerEngine by viewModel.playerEngine.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val activeAccountId by viewModel.activeAccountId.collectAsState()

    Row(modifier = Modifier.fillMaxSize().background(IptvColors.BackgroundPrimary)) {
        // لوحة التبويبات (يمين الشاشة بسبب RTL)
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .background(IptvColors.SurfaceDefault)
                .padding(vertical = 24.dp),
        ) {
            SettingsTab.entries.forEach { tab ->
                SettingsTabRow(tab = tab, isSelected = tab == selectedTab, onClick = { selectedTab = tab })
            }
        }

        // لوحة التفاصيل
        Box(modifier = Modifier.weight(1f).padding(32.dp)) {
            when (selectedTab) {
                SettingsTab.ACCOUNT -> AccountSettingsPane(
                    accounts = accounts,
                    activeAccountId = activeAccountId,
                    onAccountSelected = viewModel::onAccountSelected,
                    onAccountRemoved = viewModel::onAccountRemoved,
                )
                SettingsTab.APPEARANCE -> AppearanceSettingsPane(
                    currentAccent = accentColor,
                    onAccentSelected = viewModel::onAccentColorSelected,
                )
                SettingsTab.PLAYBACK -> PlaybackSettingsPane(
                    currentEngine = playerEngine,
                    onEngineSelected = viewModel::onPlayerEngineSelected,
                )
                SettingsTab.STORAGE -> StorageSettingsPane()
                else -> GenericSettingsPane(tab = selectedTab)
            }
        }
    }
}

@Composable
private fun SettingsTabRow(tab: SettingsTab, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) IptvColors.SurfaceElevated else IptvColors.SurfaceDefault)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .then(androidx.compose.foundation.clickable(onClick = onClick)),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            tint = if (isSelected) LocalAccentColor.current else IptvColors.TextSecondary,
        )
        Text(
            text = tab.label,
            style = LocalIptvTypography.current.body,
            color = if (isSelected) IptvColors.TextPrimary else IptvColors.TextSecondary,
        )
    }
}

@Composable
private fun AppearanceSettingsPane(currentAccent: androidx.compose.ui.graphics.Color, onAccentSelected: (androidx.compose.ui.graphics.Color) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("المظهر", style = LocalIptvTypography.current.headline, color = IptvColors.TextPrimary)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("لون التمييز (Accent)", style = LocalIptvTypography.current.title, color = IptvColors.TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IptvColors.AccentOptions.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (color == currentAccent) {
                                    Modifier.background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.001f))
                                } else {
                                    Modifier
                                },
                            )
                            .then(androidx.compose.foundation.clickable { onAccentSelected(color) }),
                    ) {
                        if (color == currentAccent) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Color.Transparent)
                                    .then(Modifier),
                            )
                        }
                    }
                }
            }
        }

        SettingsSwitchRow(title = "الوضع الداكن", description = "مفعّل دائماً على أجهزة TV", checked = true, onCheckedChange = {}, enabled = false)
        SettingsSliderRow(title = "حجم الخط", value = 1f, onValueChange = {})
    }
}

@Composable
private fun AccountSettingsPane(
    accounts: List<com.iptv.smartplayer.data.local.datastore.XtreamAccount>,
    activeAccountId: String?,
    onAccountSelected: (com.iptv.smartplayer.data.local.datastore.XtreamAccount) -> Unit,
    onAccountRemoved: (com.iptv.smartplayer.data.local.datastore.XtreamAccount) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("الحساب والاشتراك", style = LocalIptvTypography.current.headline, color = IptvColors.TextPrimary)
        Text(
            "يمكنك إضافة أكثر من حساب Xtream والتبديل بينها بسرعة",
            style = LocalIptvTypography.current.caption,
            color = IptvColors.TextSecondary,
        )

        if (accounts.isEmpty()) {
            Text("لا توجد حسابات محفوظة بعد", style = LocalIptvTypography.current.body, color = IptvColors.TextSecondary)
        } else {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(accounts) { account ->
                    AccountSwitcherCard(
                        account = account,
                        isActive = account.id == activeAccountId,
                        onClick = { onAccountSelected(account) },
                        onRemoveClick = { onAccountRemoved(account) },
                    )
                }
            }
        }
    }
}

/** بطاقة أفقية لكل حساب Xtream مُضاف — تُظهر مؤشر الحساب النشط وزر إزالة */
@Composable
private fun AccountSwitcherCard(
    account: com.iptv.smartplayer.data.local.datastore.XtreamAccount,
    isActive: Boolean,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(IptvDimens.cardCornerRadius))
            .background(if (isActive) IptvColors.SurfaceElevated else IptvColors.SurfaceDefault)
            .then(androidx.compose.foundation.clickable(onClick = onClick))
            .padding(16.dp),
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = if (isActive) LocalAccentColor.current else IptvColors.TextSecondary)
                Text(account.label, style = LocalIptvTypography.current.body, color = IptvColors.TextPrimary, maxLines = 1)
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(6.dp))
            Text(account.serverUrl, style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary, maxLines = 1)
            if (isActive) {
                Text("نشط الآن", style = LocalIptvTypography.current.caption, color = LocalAccentColor.current)
            }
        }
        androidx.compose.material3.IconButton(
            onClick = onRemoveClick,
            modifier = Modifier.align(Alignment.TopEnd).size(28.dp),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "إزالة", tint = IptvColors.TextSecondary)
        }
    }
}

@Composable
private fun PlaybackSettingsPane(
    currentEngine: com.iptv.smartplayer.core.player.PlayerEngineType,
    onEngineSelected: (com.iptv.smartplayer.core.player.PlayerEngineType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("التشغيل", style = LocalIptvTypography.current.headline, color = IptvColors.TextPrimary)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("محرك التشغيل", style = LocalIptvTypography.current.title, color = IptvColors.TextPrimary)
            Text(
                "libmpv يدعم حاويات وترميزات وملفات ترجمة أوسع من ExoPlayer، ويُنصح بتركه على \"تلقائي\" ليعمل كحل احتياطي صامت فقط عند فشل ExoPlayer.",
                style = LocalIptvTypography.current.caption,
                color = IptvColors.TextSecondary,
            )
            com.iptv.smartplayer.core.player.PlayerEngineType.entries.forEach { engine ->
                EngineOptionRow(
                    label = when (engine) {
                        com.iptv.smartplayer.core.player.PlayerEngineType.AUTO -> "تلقائي (ExoPlayer ثم libmpv عند الفشل) — موصى به"
                        com.iptv.smartplayer.core.player.PlayerEngineType.EXOPLAYER -> "ExoPlayer فقط"
                        com.iptv.smartplayer.core.player.PlayerEngineType.LIBMPV -> "libmpv فقط"
                    },
                    selected = engine == currentEngine,
                    onClick = { onEngineSelected(engine) },
                )
            }
        }

        SettingsSwitchRow(title = "فك التشفير العتادي", description = "يحسّن الأداء على معظم أجهزة TV", checked = true, onCheckedChange = {})
        SettingsSwitchRow(title = "تشغيل تلقائي للحلقة التالية", description = "ينتقل تلقائياً للحلقة التالية بعد انتهاء الحالية", checked = true, onCheckedChange = {})
        SettingsSwitchRow(title = "الترجمة الافتراضية", description = "تفعيل الترجمة تلقائياً عند توفرها", checked = false, onCheckedChange = {})
    }
}

@Composable
private fun EngineOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) IptvColors.SurfaceElevated else androidx.compose.ui.graphics.Color.Transparent)
            .then(androidx.compose.foundation.clickable(onClick = onClick))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.RadioButton(
            selected = selected,
            onClick = onClick,
            colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = LocalAccentColor.current),
        )
        Text(label, style = LocalIptvTypography.current.body, color = IptvColors.TextPrimary)
    }
}

@Composable
private fun StorageSettingsPane() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("التنزيلات والتخزين", style = LocalIptvTypography.current.headline, color = IptvColors.TextPrimary)
        Text("حجم الكاش الحالي: 128 ميجابايت", style = LocalIptvTypography.current.body, color = IptvColors.TextSecondary)
        androidx.compose.material3.Button(onClick = {}) { Text("مسح الكاش") }
    }
}

@Composable
private fun GenericSettingsPane(tab: SettingsTab) {
    Column {
        Text(tab.label, style = LocalIptvTypography.current.headline, color = IptvColors.TextPrimary)
        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
        Text("محتوى هذا القسم قيد الإكمال في مراحل التطوير التالية.", style = LocalIptvTypography.current.body, color = IptvColors.TextSecondary)
    }
}

/** مكوّن قابل لإعادة الاستخدام: عنوان + وصف + مفتاح تبديل */
@Composable
fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = LocalIptvTypography.current.title, color = IptvColors.TextPrimary)
            Text(description, style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = LocalAccentColor.current),
        )
    }
}

/** مكوّن قابل لإعادة الاستخدام: شريط انزلاقي مع معاينة حية */
@Composable
fun SettingsSliderRow(title: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text(title, style = LocalIptvTypography.current.title, color = IptvColors.TextPrimary)
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = LocalAccentColor.current, activeTrackColor = LocalAccentColor.current),
        )
    }
}
