package com.iptv.smartplayer.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.smartplayer.data.local.datastore.AccountsPreferences
import com.iptv.smartplayer.data.local.datastore.XtreamAccount
import com.iptv.smartplayer.data.remote.xtream.XtreamApi
import com.iptv.smartplayer.data.repository.XtreamCredentialsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class LoginUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSucceeded: Boolean = false,
    val savedAccounts: List<XtreamAccount> = emptyList(),
)

/**
 * يتحقق من بيانات دخول Xtream عبر استدعاء player_api.php مباشرة (بدون username/password
 * إضافيين في الرابط لأن authenticate() في XtreamApi تُرسلهما كـQuery params) ويعرض
 * user_info.status للتأكد من صلاحية الحساب (Active/Expired/Banned) قبل حفظه.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val xtreamApi: XtreamApi,
    private val accountsPreferences: AccountsPreferences,
    private val credentialsProvider: XtreamCredentialsProvider,
    private val xtreamServerProvider: com.iptv.smartplayer.core.network.XtreamServerProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState(savedAccounts = accountsPreferences.getAllAccounts()))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onServerUrlChanged(value: String) { _uiState.value = _uiState.value.copy(serverUrl = value) }
    fun onUsernameChanged(value: String) { _uiState.value = _uiState.value.copy(username = value) }
    fun onPasswordChanged(value: String) { _uiState.value = _uiState.value.copy(password = value) }

    fun login() {
        val state = _uiState.value
        if (state.serverUrl.isBlank() || state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "يرجى تعبئة كل الحقول")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // نضبط عنوان السيرفر مؤقتاً للتحقق قبل حفظ الحساب فعلياً
                xtreamServerProvider.updateServer(normalizeServerUrl(state.serverUrl))
                val response = xtreamApi.authenticate(state.username, state.password)

                when (response.userInfo?.status) {
                    "Active" -> {
                        val account = XtreamAccount(
                            id = UUID.randomUUID().toString(),
                            label = response.userInfo.username ?: state.username,
                            serverUrl = normalizeServerUrl(state.serverUrl),
                            username = state.username,
                            password = state.password,
                        )
                        accountsPreferences.addAccount(account, setActive = true)
                        credentialsProvider.refresh()
                        _uiState.value = _uiState.value.copy(isLoading = false, loginSucceeded = true)
                    }
                    "Expired" -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "انتهت صلاحية هذا الاشتراك")
                    "Banned" -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "تم حظر هذا الحساب من مزوّد الخدمة")
                    else -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "تعذّر التحقق من الحساب، تأكد من البيانات")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "تعذّر الاتصال بالسيرفر، تحقق من الرابط والاتصال بالإنترنت")
            }
        }
    }

    fun selectSavedAccount(account: XtreamAccount) {
        viewModelScope.launch {
            accountsPreferences.setActiveAccount(account.id)
            credentialsProvider.refresh()
            _uiState.value = _uiState.value.copy(loginSucceeded = true)
        }
    }

    // يقبل روابط مثل "example.com:8080" أو "http://example.com:8080/" ويوحّدها لصيغة كاملة
    private fun normalizeServerUrl(raw: String): String {
        val trimmed = raw.trim().trimEnd('/')
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) "$trimmed/" else "http://$trimmed/"
    }
}
