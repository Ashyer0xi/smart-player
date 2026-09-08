package com.iptv.smartplayer.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlaybackSettingsPane(
    currentEngine: com.iptv.smartplayer.core.player.PlayerEngineType,
    onEngineSelected: (com.iptv.smartplayer.core.player.PlayerEngineType) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val autoplay by viewModel.playbackPreferences.autoplayNext.collectAsState(initial = true)
    val prefetch by viewModel.playbackPreferences.prefetchNext.collectAsState(initial = true)

    Column {
        Text("تشغيل تلقائي للحلقة التالية")
        Switch(checked = autoplay, onCheckedChange = { viewModel.onAutoplaySelected(it) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Text("تحميل مسبق للحلقة التالية (Prefetch)")
        Switch(checked = prefetch, onCheckedChange = { viewModel.onPrefetchSelected(it) }, modifier = Modifier.fillMaxWidth())
    }
}
