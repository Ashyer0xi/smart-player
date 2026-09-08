package com.iptv.smartplayer.data.remote.tmdb

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * عميل TMDB API — لجلب البيانات الوصفية (بوسترات، خلفيات، طاقم التمثيل، المعرّفات).
 * مفتاح API يُمرَّر تلقائياً عبر Interceptor (انظر NetworkModule) بدل تكراره في كل استدعاء.
 */
interface TmdbApi {

    @GET("search/movie")
    suspend fun searchMovie(
        @Query("query") query: String,
        @Query("language") language: String = "ar",
    ): TmdbSearchResponse<TmdbMovieDto>

    @GET("search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("language") language: String = "ar",
    ): TmdbSearchResponse<TmdbTvDto>

    @GET("movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") movieId: Int,
        @Query("language") language: String = "ar",
        @Query("append_to_response") append: String = "credits,videos",
    ): TmdbMovieDetailsDto

    @GET("tv/{id}")
    suspend fun getTvDetails(
        @Path("id") tvId: Int,
        @Query("language") language: String = "ar",
        @Query("append_to_response") append: String = "credits,videos",
    ): TmdbTvDetailsDto

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("language") language: String = "ar",
    ): TmdbSearchResponse<TmdbMovieDto>

    @GET("trending/tv/week")
    suspend fun getTrendingTv(
        @Query("language") language: String = "ar",
    ): TmdbSearchResponse<TmdbTvDto>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1,
    ): TmdbSearchResponse<TmdbMovieDto>

    @GET("tv/top_rated")
    suspend fun getTopRatedTv(
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1,
    ): TmdbSearchResponse<TmdbTvDto>

    @GET("movie/{id}/similar")
    suspend fun getSimilarMovies(
        @Path("id") movieId: Int,
        @Query("language") language: String = "ar",
    ): TmdbSearchResponse<TmdbMovieDto>
}
