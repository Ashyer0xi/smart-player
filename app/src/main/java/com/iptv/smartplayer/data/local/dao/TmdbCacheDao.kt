package com.iptv.smartplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iptv.smartplayer.data.local.entity.TmdbCacheEntity

@Dao
interface TmdbCacheDao {
    @Query("SELECT * FROM tmdb_cache WHERE xtreamContentId = :xtreamContentId LIMIT 1")
    suspend fun get(xtreamContentId: String): TmdbCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: TmdbCacheEntity)

    @Query("DELETE FROM tmdb_cache WHERE cachedAtEpochMillis < :beforeEpochMillis")
    suspend fun clearOlderThan(beforeEpochMillis: Long)
}
