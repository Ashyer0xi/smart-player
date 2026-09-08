package com.iptv.smartplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** أنواع المحتوى المدعومة عبر التطبيق بالكامل — تُستخدم في عدة جداول */
enum class ContentType { LIVE_CHANNEL, MOVIE, SERIES }

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contentId: String, // stream_id أو series_id من Xtream
    val contentType: ContentType,
    val title: String,
    val posterUrl: String?,
    val addedAtEpochMillis: Long,
)
