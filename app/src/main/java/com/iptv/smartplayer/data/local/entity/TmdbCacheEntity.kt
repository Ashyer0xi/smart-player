package com.iptv.smartplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * تخزين مؤقت لنتائج TMDB مرتبطة بمعرّف Xtream (vod_id أو series_id)، لتفادي البحث المتكرر
 * في كل مرة يُفتح فيها نفس الفيلم/المسلسل — يطابق متطلب "tmdb_cache" في البرومبت.
 * rawJson يخزّن استجابة التفاصيل كاملة لتفادي إعادة تعريف كل الحقول هنا.
 */
@Entity(tableName = "tmdb_cache")
data class TmdbCacheEntity(
    @PrimaryKey val xtreamContentId: String, // "{type}_{xtream_id}"
    val tmdbId: Int,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rawJson: String,
    val cachedAtEpochMillis: Long,
) {
    /** صلاحية الكاش 7 أيام — بعدها تُعاد المزامنة مع TMDB (SyncWithTMDBUseCase) */
    fun isStale(nowEpochMillis: Long): Boolean =
        nowEpochMillis - cachedAtEpochMillis > 7 * 24 * 60 * 60 * 1000L
}
