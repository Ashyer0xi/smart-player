package com.iptv.smartplayer.presentation.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.smartplayer.core.player.PlayerEngineType
import com.iptv.smartplayer.data.local.datastore.AccountsPreferences
import com.iptv.smartplayer.data.local.datastore.PlaybackPreferences
import com.iptv.smartplayer.data.local.datastore.XtreamAccount
import com.iptv.smartplayer.data.repository.XtreamCredentialsProvider
import com.iptv.smartplayer.presentation.theme.IptvColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * يدير لون التمييز (Accent) ومحرك التشغيل والحسابات المحفوظة — كلها محفوظة عبر
 * DataStore/الملف المشفَّر لتبقى بين الجلسات.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val playbackPreferences: PlaybackPreferences,
    private val accountsPreferences: AccountsPreferences,
    private val credentialsProvider: XtreamCredentialsProvider,
) : ViewModel() {

    private val _accentColor = MutableStateFlow(IptvColors.AccentPrimaryDefault)
    val accentColor: StateFlow<Color> = _accentColor.asStateFlow()

    private val _playerEngine = MutableStateFlow(PlayerEngineType.AUTO)
    val playerEngine: StateFlow<PlayerEngineType> = _playerEngine.asStateFlow()

    private val _accounts = MutableStateFlow<List<XtreamAccount>>(emptyList())
    val accounts: StateFlow<List<XtreamAccount>> = _accounts.asStateFlow()

    private val _activeAccountId = MutableStateFlow<String?>(null)
    val activeAccountId: StateFlow<String?> = _activeAccountId.asStateFlow()

    init {
        viewModelScope.launch {
            playbackPreferences.playerEngine.collect { _playerEngine.value = it }
        }
        viewModelScope.launch {
            accountsPreferences.activeAccountId.collect { _activeAccountId.value = it }
        }
        refreshAccounts()
    }

    private fun refreshAccounts() {
        _accounts.value = accountsPreferences.getAllAccounts()
    }

    fun onAccentColorSelected(color: Color) {
        _accentColor.value = color
        // TODO: حفظ لون التمييز أيضاً عبر playbackPreferences.setAccentColorArgb عند إضافة تحويل Color<->Long
    }

    fun onPlayerEngineSelected(engine: PlayerEngineType) {
        _playerEngine.value = engine
        viewModelScope.launch { playbackPreferences.setPlayerEngine(engine) }
    }

    fun onAccountSelected(account: XtreamAccount) {
        viewModelScope.launch {
            accountsPreferences.setActiveAccount(account.id)
            credentialsProvider.refresh()
        }
    }

    fun onAccountRemoved(account: XtreamAccount) {
        viewModelScope.launch {
            accountsPreferences.removeAccount(account.id)
            refreshAccounts()
        }
    }
}
