package com.iptv.smartplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * جدول "أكمل المشاهدة" — يخزّن آخر نقطة توقف لكل عنصر (فيلم أو حلقة مسلسل).
 * seasonNumber/episodeNumber تبقى null بالنسبة للأفلام.
 */
@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val contentId: String, // stream_id أو "{series_id}_{season}_{episode}"
    val contentType: ContentType,
    val title: String,
    val posterUrl: String?,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val positionMillis: Long,
    val durationMillis: Long,
    val lastWatchedEpochMillis: Long,
) {
    val progressFraction: Float
        get() = if (durationMillis > 0) (positionMillis.toFloat() / durationMillis).coerceIn(0f, 1f) else 0f

    /** يُعتبر "مكتملاً" إن تجاوزت المشاهدة 92% — يُستبعد عندها من صف "أكمل المشاهدة" */
    val isCompleted: Boolean
        get() = progressFraction >= 0.92f
}
