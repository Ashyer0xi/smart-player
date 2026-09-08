package com.iptv.smartplayer.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptv.smartplayer.data.local.datastore.XtreamAccount
import com.iptv.smartplayer.presentation.components.FocusableCard
import com.iptv.smartplayer.presentation.theme.IptvColors
import com.iptv.smartplayer.presentation.theme.LocalAccentColor
import com.iptv.smartplayer.presentation.theme.LocalIptvTypography

/**
 * شاشة تسجيل الدخول — أول ما يراه المستخدم إن لم يوجد حساب Xtream محفوظ.
 * إن وُجدت حسابات محفوظة مسبقاً (تبديل حسابات متعددة)، تُعرض كبطاقات سريعة أعلى النموذج.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.loginSucceeded) {
        if (state.loginSucceeded) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IptvColors.BackgroundPrimary)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.width(480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("تسجيل الدخول", style = LocalIptvTypography.current.display, color = IptvColors.TextPrimary)
            Text(
                "أدخل بيانات اشتراك Xtream Codes الخاص بك",
                style = LocalIptvTypography.current.body,
                color = IptvColors.TextSecondary,
                textAlign = TextAlign.Center,
            )

            if (state.savedAccounts.isNotEmpty()) {
                Text("الحسابات المحفوظة", style = LocalIptvTypography.current.title, color = IptvColors.TextPrimary, modifier = Modifier.fillMaxWidth())
                LazyColumn(modifier = Modifier.height(120.dp)) {
                    items(state.savedAccounts) { account ->
                        SavedAccountRow(account = account, onClick = { viewModel.selectSavedAccount(account) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("— أو أضف حساباً جديداً —", style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary)
                }
            }

            OutlinedTextField(
                value = state.serverUrl,
                onValueChange = viewModel::onServerUrlChanged,
                label = { Text("عنوان السيرفر (مثال: example.com:8080)") },
                leadingIcon = { Icon(Icons.Filled.Dns, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LocalAccentColor.current),
            )
            OutlinedTextField(
                value = state.username,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("اسم المستخدم") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LocalAccentColor.current),
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("كلمة المرور") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LocalAccentColor.current),
            )

            state.errorMessage?.let {
                Text(it, style = LocalIptvTypography.current.caption, color = IptvColors.StateError, textAlign = TextAlign.Center)
            }

            Button(
                onClick = viewModel::login,
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = LocalAccentColor.current),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), color = IptvColors.TextPrimary)
                } else {
                    Text("تسجيل الدخول")
                }
            }
        }
    }
}

@Composable
private fun SavedAccountRow(account: XtreamAccount, onClick: () -> Unit) {
    FocusableCard(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp)) { _ ->
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = LocalAccentColor.current)
            Column {
                Text(account.label, style = LocalIptvTypography.current.body, color = IptvColors.TextPrimary)
                Text(account.serverUrl, style = LocalIptvTypography.current.caption, color = IptvColors.TextSecondary)
            }
        }
    }
}
