package com.iptv.smartplayer.data.local.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.core.DataStore
import com.iptv.smartplayer.core.player.PlayerEngineType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تفضيلات التشغيل المحفوظة عبر DataStore — تشمل محرك التشغيل المختار من شاشة الإعدادات
 * ولون التمييز (Accent)، لتبقى محفوظة بين جلسات التطبيق.
 */
@Singleton
class PlaybackPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val PLAYER_ENGINE = stringPreferencesKey("player_engine")
        val ACCENT_COLOR_ARGB = longPreferencesKey("accent_color_argb")
    }

    val playerEngine: Flow<PlayerEngineType> = dataStore.data.map { prefs ->
        prefs[Keys.PLAYER_ENGINE]?.let { runCatching { PlayerEngineType.valueOf(it) }.getOrNull() }
            ?: PlayerEngineType.AUTO
    }

    suspend fun setPlayerEngine(engine: PlayerEngineType) {
        dataStore.edit { it[Keys.PLAYER_ENGINE] = engine.name }
    }

    val accentColorArgb: Flow<Long?> = dataStore.data.map { it[Keys.ACCENT_COLOR_ARGB] }

    suspend fun setAccentColorArgb(argb: Long) {
        dataStore.edit { it[Keys.ACCENT_COLOR_ARGB] = argb }
    }
}
