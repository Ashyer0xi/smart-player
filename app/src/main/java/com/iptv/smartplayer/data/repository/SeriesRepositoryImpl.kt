package com.iptv.smartplayer.data.repository

import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.data.remote.tmdb.TmdbImageUrlBuilder
import com.iptv.smartplayer.data.remote.xtream.XtreamApi
import com.iptv.smartplayer.domain.model.ContentCategory
import com.iptv.smartplayer.domain.model.Episode
import com.iptv.smartplayer.domain.model.Season
import com.iptv.smartplayer.domain.model.Series
import com.iptv.smartplayer.domain.model.SeriesStatus
import com.iptv.smartplayer.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * هيكل تطبيق Repository المسلسلات — نفس نمط MovieRepositoryImpl (Xtream للتشغيل + TMDB للوصف).
 * منطق مطابقة tmdb_id لكل مسلسل يُستكمل في مرحلة التكامل الكاملة (العقل الثالث).
 */
class SeriesRepositoryImpl @Inject constructor(
    private val xtreamApi: XtreamApi,
    private val credentialsProvider: XtreamCredentialsProvider,
) : SeriesRepository {

    override fun getCategories(): Flow<Resource<List<ContentCategory>>> = flow {
        emit(Resource.Loading())
        try {
            val creds = credentialsProvider.current()
            val categories = xtreamApi.getSeriesCategories(creds.username, creds.password)
                .map { ContentCategory(id = it.categoryId, name = it.categoryName) }
            emit(Resource.Success(categories))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب التصنيفات"))
        }
    }

    override fun getSeriesList(categoryId: String?): Flow<Resource<List<Series>>> = flow {
        emit(Resource.Loading())
        try {
            val creds = credentialsProvider.current()
            val list = xtreamApi.getSeries(creds.username, creds.password, categoryId).map { dto ->
                Series(
                    xtreamSeriesId = dto.seriesId,
                    tmdbId = null,
                    title = dto.name,
                    overview = dto.plot,
                    posterUrl = dto.cover,
                    backdropUrl = null,
                    rating = dto.rating?.toDoubleOrNull(),
                    genres = emptyList(),
                    numberOfSeasons = 0,
                    status = SeriesStatus.UNKNOWN,
                )
            }
            emit(Resource.Success(list))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب المسلسلات"))
        }
    }

    override fun getSeriesDetails(seriesId: Int): Flow<Resource<Series>> = flow {
        emit(Resource.Loading())
        try {
            val creds = credentialsProvider.current()
            val info = xtreamApi.getSeriesInfo(creds.username, creds.password, seriesId.toString())
            val seasonsCount = info.episodes?.keys?.size ?: 0
            emit(
                Resource.Success(
                    Series(
                        xtreamSeriesId = seriesId,
                        tmdbId = null,
                        title = info.info?.name ?: "",
                        overview = info.info?.plot,
                        posterUrl = info.info?.cover,
                        backdropUrl = null,
                        rating = info.info?.rating?.toDoubleOrNull(),
                        genres = emptyList(),
                        numberOfSeasons = seasonsCount,
                        status = SeriesStatus.UNKNOWN,
                    ),
                ),
            )
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب تفاصيل المسلسل"))
        }
    }

    override fun getSeasons(seriesId: Int): Flow<Resource<List<Season>>> = flow {
        emit(Resource.Loading())
        try {
            val creds = credentialsProvider.current()
            val info = xtreamApi.getSeriesInfo(creds.username, creds.password, seriesId.toString())
            val seasons = info.episodes?.map { (seasonNumber, episodes) ->
                Season(
                    seasonNumber = seasonNumber.toIntOrNull() ?: 0,
                    episodes = episodes.map { ep ->
                        Episode(
                            id = ep.id,
                            seasonNumber = ep.season,
                            episodeNumber = ep.episodeNum,
                            title = ep.title ?: "الحلقة ${ep.episodeNum}",
                            plot = ep.info?.plot,
                            thumbnailUrl = ep.info?.thumbnail,
                            durationMinutes = ep.info?.duration?.toIntOrNull(),
                            containerExtension = ep.containerExtension ?: "mp4",
                        )
                    },
                )
            }?.sortedBy { it.seasonNumber } ?: emptyList()
            emit(Resource.Success(seasons))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب المواسم"))
        }
    }
}
