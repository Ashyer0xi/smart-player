package com.iptv.smartplayer.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** حساب Xtream واحد — يدعم "تبديل الحسابات المتعددة" المذكور في شاشة الإعدادات */
data class XtreamAccount(
    val id: String,
    val label: String,
    val serverUrl: String,
    val username: String,
    val password: String,
)

private data class AccountsFile(val accounts: List<XtreamAccount> = emptyList(), val activeAccountId: String? = null)

/**
 * يخزّن حسابات Xtream (تتضمن كلمات مرور) بشكل مشفَّر عبر EncryptedFile من مكتبة
 * androidx.security-crypto، تطبيقاً لبند "تحليل الأمان" في البرومبت: "عدم تخزين بيانات
 * الدخول بشكل نصي" و"استخدام EncryptedSharedPreferences/EncryptedFile للحساسيات".
 * DataStore العادي يُستخدم فقط لتخزين "أي حساب نشط حالياً" (معرّف غير حسّاس).
 */
@Singleton
class AccountsPreferences @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val ACTIVE_ACCOUNT_ID = stringPreferencesKey("active_account_id")
    }

    private val gson = Gson()
    private val encryptedFile: File get() = File(context.filesDir, "xtream_accounts.enc")

    private fun masterKey() = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private fun readAccountsFile(): AccountsFile {
        val file = encryptedFile
        if (!file.exists()) return AccountsFile()
        return try {
            val encrypted = EncryptedFile.Builder(context, file, masterKey(), EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB).build()
            encrypted.openFileInput().use { input -> gson.fromJson(input.reader().readText(), AccountsFile::class.java) }
        } catch (e: Exception) {
            AccountsFile()
        }
    }

    private fun writeAccountsFile(data: AccountsFile) {
        val file = encryptedFile
        if (file.exists()) file.delete() // EncryptedFile يرفض الكتابة فوق ملف موجود
        val encrypted = EncryptedFile.Builder(context, file, masterKey(), EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB).build()
        encrypted.openFileOutput().use { output -> output.write(gson.toJson(data).toByteArray()) }
    }

    fun getAllAccounts(): List<XtreamAccount> = readAccountsFile().accounts

    fun getActiveAccountIdBlocking(): String? = readAccountsFile().activeAccountId

    val activeAccountId: Flow<String?> = dataStore.data.map { it[Keys.ACTIVE_ACCOUNT_ID] }

    suspend fun addAccount(account: XtreamAccount, setActive: Boolean = true) {
        val current = readAccountsFile()
        writeAccountsFile(current.copy(accounts = current.accounts + account))
        if (setActive) setActiveAccount(account.id)
    }

    suspend fun removeAccount(accountId: String) {
        val current = readAccountsFile()
        writeAccountsFile(current.copy(accounts = current.accounts.filterNot { it.id == accountId }))
    }

    suspend fun setActiveAccount(accountId: String) {
        dataStore.edit { it[Keys.ACTIVE_ACCOUNT_ID] = accountId }
    }

    fun getAccount(accountId: String): XtreamAccount? = readAccountsFile().accounts.firstOrNull { it.id == accountId }
}
