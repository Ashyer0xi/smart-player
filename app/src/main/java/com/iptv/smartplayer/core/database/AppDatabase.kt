package com.iptv.smartplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iptv.smartplayer.data.local.dao.ChannelDao
import com.iptv.smartplayer.data.local.dao.CustomCatalogDao
import com.iptv.smartplayer.data.local.dao.EpgDao
import com.iptv.smartplayer.data.local.dao.FavoriteDao
import com.iptv.smartplayer.data.local.dao.TmdbCacheDao
import com.iptv.smartplayer.data.local.dao.WatchHistoryDao
import com.iptv.smartplayer.data.local.entity.CatalogItemEntity
import com.iptv.smartplayer.data.local.entity.ChannelEntity
import com.iptv.smartplayer.data.local.entity.CustomCatalogEntity
import com.iptv.smartplayer.data.local.entity.EpgEntity
import com.iptv.smartplayer.data.local.entity.FavoriteEntity
import com.iptv.smartplayer.data.local.entity.TmdbCacheEntity
import com.iptv.smartplayer.data.local.entity.WatchHistoryEntity

@Database(
    entities = [
        FavoriteEntity::class,
        WatchHistoryEntity::class,
        CustomCatalogEntity::class,
        CatalogItemEntity::class,
        ChannelEntity::class,
        EpgEntity::class,
        TmdbCacheEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun customCatalogDao(): CustomCatalogDao
    abstract fun channelDao(): ChannelDao
    abstract fun epgDao(): EpgDao
    abstract fun tmdbCacheDao(): TmdbCacheDao

    companion object {
        const val DATABASE_NAME = "iptv_smart_player.db"
    }
}
