package com.iptv.smartplayer.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.smartplayer.data.local.datastore.AccountsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * يقرر شاشة الانطلاق: تسجيل الدخول (لا يوجد حساب محفوظ) أو الرئيسية مباشرة (يوجد حساب نشط).
 * القراءة من الملف المشفَّر تُنفَّذ على Dispatchers.IO لتفادي أي تجميد للواجهة عند بدء التطبيق.
 */
@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    private val accountsPreferences: AccountsPreferences,
) : ViewModel() {

    private val _startRoute = MutableStateFlow<String?>(null)
    val startRoute: StateFlow<String?> = _startRoute.asStateFlow()

    init {
        viewModelScope.launch {
            val hasActiveAccount = withContext(Dispatchers.IO) {
                accountsPreferences.getActiveAccountIdBlocking() != null
            }
            _startRoute.value = if (hasActiveAccount) Screen.Home.route else Screen.Login.route
        }
    }
}
