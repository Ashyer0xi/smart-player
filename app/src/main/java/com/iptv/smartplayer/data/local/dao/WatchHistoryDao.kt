package com.iptv.smartplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iptv.smartplayer.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    /** يُستخدم لصف "أكمل المشاهدة" في الشاشة الرئيسية — يستبعد العناصر المكتملة عبر isCompleted في الطبقة الأعلى */
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedEpochMillis DESC LIMIT 20")
    fun observeRecentlyWatched(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE contentId = :contentId LIMIT 1")
    suspend fun getByContentId(contentId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE contentId = :contentId")
    suspend fun delete(contentId: String)
}
