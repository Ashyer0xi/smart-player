package com.iptv.smartplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iptv.smartplayer.data.local.entity.ChannelEntity
import com.iptv.smartplayer.data.local.entity.EpgEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE isHidden = 0 ORDER BY sortOrder ASC")
    fun observeVisibleChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE categoryId = :categoryId AND isHidden = 0 ORDER BY sortOrder ASC")
    fun observeChannelsByCategory(categoryId: String): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("UPDATE channels SET isHidden = :hidden WHERE streamId = :streamId")
    suspend fun setHidden(streamId: Int, hidden: Boolean)

    @Query("UPDATE channels SET sortOrder = :order WHERE streamId = :streamId")
    suspend fun updateSortOrder(streamId: Int, order: Int)
}

@Dao
interface EpgDao {
    @Query(
        "SELECT * FROM epg_data WHERE channelId = :channelId " +
            "AND endEpochMillis >= :nowEpochMillis ORDER BY startEpochMillis ASC LIMIT :limit",
    )
    fun observeUpcoming(channelId: String, nowEpochMillis: Long, limit: Int = 10): Flow<List<EpgEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<EpgEntity>)

    @Query("DELETE FROM epg_data WHERE endEpochMillis < :beforeEpochMillis")
    suspend fun clearExpired(beforeEpochMillis: Long)
}
