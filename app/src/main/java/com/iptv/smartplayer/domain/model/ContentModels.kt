package com.iptv.smartplayer.domain.model

/** نموذج فيلم موحّد — يدمج بيانات Xtream (رابط التشغيل) مع بيانات TMDB (الوصف، البوستر، التقييم) */
data class Movie(
    val xtreamStreamId: Int,
    val tmdbId: Int?,
    val title: String,
    val overview: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseYear: String?,
    val rating: Double?,
    val genres: List<String>,
    val durationMinutes: Int?,
    val containerExtension: String,
    val cast: List<CastMember> = emptyList(),
    val trailerYoutubeKey: String? = null,
)

data class Series(
    val xtreamSeriesId: Int,
    val tmdbId: Int?,
    val title: String,
    val overview: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rating: Double?,
    val genres: List<String>,
    val numberOfSeasons: Int,
    val status: SeriesStatus,
    val cast: List<CastMember> = emptyList(),
)

enum class SeriesStatus { RETURNING, ENDED, UNKNOWN }

data class Season(
    val seasonNumber: Int,
    val episodes: List<Episode>,
)

data class Episode(
    val id: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val plot: String?,
    val thumbnailUrl: String?,
    val durationMinutes: Int?,
    val containerExtension: String,
    val watchProgressFraction: Float = 0f,
)

data class CastMember(
    val name: String,
    val character: String?,
    val profileUrl: String?,
)

data class Channel(
    val streamId: Int,
    val name: String,
    val logoUrl: String?,
    val categoryId: String?,
    val currentProgramTitle: String? = null,
    val supportsCatchup: Boolean = false,
)

data class EpgProgram(
    val title: String,
    val description: String?,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
) {
    fun isCurrentlyAiring(nowEpochMillis: Long): Boolean =
        nowEpochMillis in startEpochMillis until endEpochMillis
}

data class ContentCategory(
    val id: String,
    val name: String,
)
