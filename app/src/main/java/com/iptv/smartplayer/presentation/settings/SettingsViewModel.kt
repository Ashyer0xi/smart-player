package com.iptv.smartplayer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.smartplayer.data.local.datastore.PlaybackPreferences
import com.iptv.smartplayer.data.local.datastore.AccountsPreferences
import com.iptv.smartplayer.data.local.datastore.XtreamAccount
import com.iptv.smartplayer.data.repository.XtreamCredentialsProvider
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val playbackPreferences: PlaybackPreferences,
    private val accountsPreferences: AccountsPreferences,
    private val credentialsProvider: XtreamCredentialsProvider,
) : ViewModel() {

    fun onAutoplaySelected(enabled: Boolean) {
        viewModelScope.launch { playbackPreferences.setAutoplayNext(enabled) }
    }

    fun onPrefetchSelected(enabled: Boolean) {
        viewModelScope.launch { playbackPreferences.setPrefetchNext(enabled) }
    }

    // Existing APIs proxied below for compatibility with the SettingsScreen previously using this ViewModel
    fun getAllAccounts(): List<XtreamAccount> = accountsPreferences.getAllAccounts()
    fun setActiveAccount(id: String) { viewModelScope.launch { accountsPreferences.setActiveAccount(id) } }
    fun removeAccount(id: String) { viewModelScope.launch { accountsPreferences.removeAccount(id) } }
}
