package com.iptv.smartplayer.data.repository

import com.iptv.smartplayer.data.local.dao.FavoriteDao
import com.iptv.smartplayer.data.local.dao.WatchHistoryDao
import com.iptv.smartplayer.data.local.entity.ContentType
import com.iptv.smartplayer.data.local.entity.FavoriteEntity
import com.iptv.smartplayer.data.local.entity.WatchHistoryEntity
import com.iptv.smartplayer.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LibraryRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val watchHistoryDao: WatchHistoryDao,
) : LibraryRepository {

    override fun observeContinueWatching(): Flow<List<WatchHistoryEntity>> =
        watchHistoryDao.observeRecentlyWatched()

    override fun observeFavorites(): Flow<List<FavoriteEntity>> =
        favoriteDao.observeAll()

    override suspend fun saveWatchProgress(
        contentId: String,
        contentType: ContentType,
        title: String,
        posterUrl: String?,
        positionMillis: Long,
        durationMillis: Long,
        seasonNumber: Int?,
        episodeNumber: Int?,
    ) {
        watchHistoryDao.upsert(
            WatchHistoryEntity(
                contentId = contentId,
                contentType = contentType,
                title = title,
                posterUrl = posterUrl,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                positionMillis = positionMillis,
                durationMillis = durationMillis,
                lastWatchedEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override fun observeIsFavorite(contentId: String): Flow<Boolean> =
        favoriteDao.observeIsFavorite(contentId)

    override suspend fun toggleFavorite(
        contentId: String,
        contentType: ContentType,
        title: String,
        posterUrl: String?,
    ) {
        val isFavorite = favoriteDao.observeIsFavorite(contentId).first()
        if (isFavorite) {
            favoriteDao.deleteByContentId(contentId)
        } else {
            favoriteDao.insert(
                FavoriteEntity(
                    contentId = contentId,
                    contentType = contentType,
                    title = title,
                    posterUrl = posterUrl,
                    addedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
    }
}
