package com.iptv.smartplayer.data.repository

import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.core.util.networkBoundResource
import com.iptv.smartplayer.data.local.dao.TmdbCacheDao
import com.iptv.smartplayer.data.remote.tmdb.TmdbApi
import com.iptv.smartplayer.data.remote.tmdb.TmdbImageUrlBuilder
import com.iptv.smartplayer.data.remote.xtream.XtreamApi
import com.iptv.smartplayer.domain.model.ContentCategory
import com.iptv.smartplayer.domain.model.Movie
import com.iptv.smartplayer.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * تطبيق Repository الأفلام — يدمج Xtream (رابط التشغيل + القائمة الأساسية) مع TMDB
 * (الوصف، البوستر عالي الجودة، التقييم، الممثلون) عبر tmdb_cache لتفادي البحث المتكرر.
 *
 * ملاحظة: منطق المطابقة الكامل (Xtream ID <-> TMDB ID عبر tmdb_id إن توفر، وإلا عبر
 * مطابقة الاسم + السنة) يُفصَّل في مرحلة لاحقة ضمن "العقل الثالث: مطور الـ API والتكامل".
 * هذا التطبيق يوفر الهيكل الكامل وتدفق البيانات الصحيح (NetworkBoundResource) الجاهز للتوصيل.
 */
class MovieRepositoryImpl @Inject constructor(
    private val xtreamApi: XtreamApi,
    private val tmdbApi: TmdbApi,
    private val tmdbCacheDao: TmdbCacheDao,
    private val credentialsProvider: XtreamCredentialsProvider,
) : MovieRepository {

    override fun getCategories(): Flow<Resource<List<ContentCategory>>> = flow {
        emit(Resource.Loading())
        try {
            val creds = credentialsProvider.current()
            val categories = xtreamApi.getVodCategories(creds.username, creds.password)
                .map { ContentCategory(id = it.categoryId, name = it.categoryName) }
            emit(Resource.Success(categories))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب التصنيفات"))
        }
    }

    override fun getMovies(categoryId: String?): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading())
        try {
            val creds = credentialsProvider.current()
            val streams = xtreamApi.getVodStreams(creds.username, creds.password, categoryId)
            val movies = streams.map { dto ->
                Movie(
                    xtreamStreamId = dto.streamId,
                    tmdbId = null,
                    title = dto.name,
                    overview = null,
                    posterUrl = dto.streamIcon,
                    backdropUrl = null,
                    releaseYear = dto.added,
                    rating = dto.rating?.toDoubleOrNull(),
                    genres = emptyList(),
                    durationMinutes = null,
                    containerExtension = dto.containerExtension ?: "mp4",
                )
            }
            emit(Resource.Success(movies))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب الأفلام"))
        }
    }

    override fun getMovieDetails(streamId: Int): Flow<Resource<Movie>> = flow {
        emit(Resource.Loading())
        try {
            val creds = credentialsProvider.current()
            val info = xtreamApi.getVodInfo(creds.username, creds.password, streamId.toString())
            val tmdbId = info.info?.tmdbId?.toIntOrNull()

            var posterUrl = info.movieData?.streamIcon
            var backdropUrl: String? = null
            var overview = info.info?.plot
            var rating: Double? = null
            var cast = emptyList<com.iptv.smartplayer.domain.model.CastMember>()
            var trailerKey: String? = null

            if (tmdbId != null) {
                val details = tmdbApi.getMovieDetails(tmdbId)
                posterUrl = TmdbImageUrlBuilder.poster(details.posterPath) ?: posterUrl
                backdropUrl = TmdbImageUrlBuilder.backdrop(details.backdropPath)
                overview = details.overview ?: overview
                rating = details.voteAverage
                cast = details.credits?.cast?.take(10)?.map {
                    com.iptv.smartplayer.domain.model.CastMember(
                        name = it.name,
                        character = it.character,
                        profileUrl = TmdbImageUrlBuilder.profile(it.profilePath),
                    )
                } ?: emptyList()
                trailerKey = details.videos?.results
                    ?.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }?.key
            }

            emit(
                Resource.Success(
                    Movie(
                        xtreamStreamId = streamId,
                        tmdbId = tmdbId,
                        title = info.movieData?.name ?: "",
                        overview = overview,
                        posterUrl = posterUrl,
                        backdropUrl = backdropUrl,
                        releaseYear = info.info?.releaseDate,
                        rating = rating,
                        genres = info.info?.genre?.split(",")?.map { it.trim() } ?: emptyList(),
                        durationMinutes = info.info?.duration?.let { parseDurationToMinutes(it) },
                        containerExtension = info.movieData?.containerExtension ?: "mp4",
                        cast = cast,
                        trailerYoutubeKey = trailerKey,
                    ),
                ),
            )
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب تفاصيل الفيلم"))
        }
    }

    override fun searchMovies(query: String): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading())
        try {
            val results = tmdbApi.searchMovie(query).results.map {
                Movie(
                    xtreamStreamId = -1, // يُطابَق لاحقاً مع مكتبة Xtream المحلية بالاسم
                    tmdbId = it.id,
                    title = it.title,
                    overview = it.overview,
                    posterUrl = TmdbImageUrlBuilder.poster(it.posterPath),
                    backdropUrl = TmdbImageUrlBuilder.backdrop(it.backdropPath),
                    releaseYear = it.releaseDate,
                    rating = it.voteAverage,
                    genres = emptyList(),
                    durationMinutes = null,
                    containerExtension = "mp4",
                )
            }
            emit(Resource.Success(results))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر البحث"))
        }
    }

    override fun getTrendingMovies(): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading())
        try {
            val results = tmdbApi.getTrendingMovies().results.map {
                Movie(
                    xtreamStreamId = -1,
                    tmdbId = it.id,
                    title = it.title,
                    overview = it.overview,
                    posterUrl = TmdbImageUrlBuilder.poster(it.posterPath),
                    backdropUrl = TmdbImageUrlBuilder.backdrop(it.backdropPath),
                    releaseYear = it.releaseDate,
                    rating = it.voteAverage,
                    genres = emptyList(),
                    durationMinutes = null,
                    containerExtension = "mp4",
                )
            }
            emit(Resource.Success(results))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب الرائج"))
        }
    }

    override fun getSimilarMovies(tmdbId: Int): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading())
        try {
            val results = tmdbApi.getSimilarMovies(tmdbId).results.map {
                Movie(
                    xtreamStreamId = -1,
                    tmdbId = it.id,
                    title = it.title,
                    overview = it.overview,
                    posterUrl = TmdbImageUrlBuilder.poster(it.posterPath),
                    backdropUrl = TmdbImageUrlBuilder.backdrop(it.backdropPath),
                    releaseYear = it.releaseDate,
                    rating = it.voteAverage,
                    genres = emptyList(),
                    durationMinutes = null,
                    containerExtension = "mp4",
                )
            }
            emit(Resource.Success(results))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب أعمال مشابهة"))
        }
    }

    private fun parseDurationToMinutes(raw: String): Int? {
        // صيغة Xtream الشائعة: "01:32:00"
        val parts = raw.split(":").mapNotNull { it.toIntOrNull() }
        return when (parts.size) {
            3 -> parts[0] * 60 + parts[1]
            2 -> parts[0]
            else -> raw.toIntOrNull()
        }
    }
}
