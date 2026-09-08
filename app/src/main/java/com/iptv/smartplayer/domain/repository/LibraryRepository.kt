package com.iptv.smartplayer.domain.repository

import com.iptv.smartplayer.data.local.entity.ContentType
import com.iptv.smartplayer.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * يغطي المفضلة وسجل المشاهدة والكتالوجات المخصصة — بيانات محلية بالكامل (Room)
 * ولا تحتاج NetworkBoundResource لأنها لا تُجلب من أي API خارجي.
 */
interface LibraryRepository {
    fun observeContinueWatching(): Flow<List<WatchHistoryEntity>>
    fun observeFavorites(): Flow<List<com.iptv.smartplayer.data.local.entity.FavoriteEntity>>
    suspend fun saveWatchProgress(
        contentId: String,
        contentType: ContentType,
        title: String,
        posterUrl: String?,
        positionMillis: Long,
        durationMillis: Long,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
    )

    fun observeIsFavorite(contentId: String): Flow<Boolean>
    suspend fun toggleFavorite(contentId: String, contentType: ContentType, title: String, posterUrl: String?)
}
