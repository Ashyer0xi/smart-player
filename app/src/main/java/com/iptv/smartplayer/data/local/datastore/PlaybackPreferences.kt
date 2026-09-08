package com.iptv.smartplayer.data.local.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.core.DataStore
import com.iptv.smartplayer.core.player.PlayerEngineType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val PLAYER_ENGINE = stringPreferencesKey("player_engine")
        val ACCENT_COLOR_ARGB = longPreferencesKey("accent_color_argb")

        // New keys
        val AUTOPLAY_NEXT = booleanPreferencesKey("autoplay_next")
        val PREFETCH_NEXT = booleanPreferencesKey("prefetch_next")
        val CACHE_SIZE_BYTES = longPreferencesKey("cache_size_bytes")
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

    // New preferences flows & setters (with defaults)
    val autoplayNext: Flow<Boolean> = dataStore.data.map { it[Keys.AUTOPLAY_NEXT] ?: true }
    suspend fun setAutoplayNext(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTOPLAY_NEXT] = enabled }
    }

    val prefetchNext: Flow<Boolean> = dataStore.data.map { it[Keys.PREFETCH_NEXT] ?: true }
    suspend fun setPrefetchNext(enabled: Boolean) {
        dataStore.edit { it[Keys.PREFETCH_NEXT] = enabled }
    }

    val cacheSizeBytes: Flow<Long> = dataStore.data.map { it[Keys.CACHE_SIZE_BYTES] ?: (200L * 1024L * 1024L) }
    suspend fun setCacheSizeBytes(bytes: Long) {
        dataStore.edit { it[Keys.CACHE_SIZE_BYTES] = bytes }
    }
}
