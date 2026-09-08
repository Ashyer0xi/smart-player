package com.iptv.smartplayer.data.remote.tmdb

import com.google.gson.annotations.SerializedName

data class TmdbSearchResponse<T>(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<T>,
    @SerializedName("total_pages") val totalPages: Int,
)

data class TmdbMovieDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("genre_ids") val genreIds: List<Int>?,
)

data class TmdbTvDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("genre_ids") val genreIds: List<Int>?,
)

data class TmdbMovieDetailsDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("runtime") val runtimeMinutes: Int?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("genres") val genres: List<TmdbGenreDto>?,
    @SerializedName("credits") val credits: TmdbCreditsDto?,
    @SerializedName("videos") val videos: TmdbVideosDto?,
)

data class TmdbTvDetailsDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("number_of_seasons") val numberOfSeasons: Int?,
    @SerializedName("status") val status: String?, // "Returning Series" / "Ended"
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("genres") val genres: List<TmdbGenreDto>?,
    @SerializedName("credits") val credits: TmdbCreditsDto?,
    @SerializedName("videos") val videos: TmdbVideosDto?,
)

data class TmdbGenreDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
)

data class TmdbCreditsDto(
    @SerializedName("cast") val cast: List<TmdbCastDto>?,
)

data class TmdbCastDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String?,
    @SerializedName("profile_path") val profilePath: String?,
)

data class TmdbVideosDto(
    @SerializedName("results") val results: List<TmdbVideoDto>?,
)

data class TmdbVideoDto(
    @SerializedName("key") val key: String, // YouTube video key
    @SerializedName("site") val site: String?,
    @SerializedName("type") val type: String?, // "Trailer" / "Teaser"
)

/** يبني روابط الصور الكاملة من TMDB — الأحجام القياسية الموصى بها لكل استخدام */
object TmdbImageUrlBuilder {
    private const val BASE_URL = "https://image.tmdb.org/t/p/"

    fun poster(path: String?, size: String = "w500"): String? =
        path?.let { "$BASE_URL$size$it" }

    fun backdrop(path: String?, size: String = "w1280"): String? =
        path?.let { "$BASE_URL$size$it" }

    fun profile(path: String?, size: String = "w185"): String? =
        path?.let { "$BASE_URL$size$it" }
}
