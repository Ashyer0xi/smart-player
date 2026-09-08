package com.iptv.smartplayer.data.remote.xtream

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * عميل Xtream Codes API — يغطي كل الـ endpoints الموثّقة في البرومبت.
 * ملاحظة: Xtream Codes يستخدم نفس المسار (player_api.php) مع باراميتر action مختلف،
 * لذا كل الدوال هنا تستهدف نفس المسار عبر @Query بدل @Path منفصلة.
 */
interface XtreamApi {

    // ---------- المصادقة / معلومات الحساب ----------
    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String,
    ): XtreamAuthResponse

    // ---------- القنوات المباشرة ----------
    @GET("player_api.php?action=get_live_categories")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
    ): List<XtreamCategoryDto>

    @GET("player_api.php?action=get_live_streams")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null,
    ): List<XtreamLiveStreamDto>

    // ---------- الأفلام (VOD) ----------
    @GET("player_api.php?action=get_vod_categories")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
    ): List<XtreamCategoryDto>

    @GET("player_api.php?action=get_vod_streams")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null,
    ): List<XtreamVodStreamDto>

    @GET("player_api.php?action=get_vod_info")
    suspend fun getVodInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("vod_id") vodId: String,
    ): XtreamVodInfoDto

    // ---------- المسلسلات ----------
    @GET("player_api.php?action=get_series_categories")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
    ): List<XtreamCategoryDto>

    @GET("player_api.php?action=get_series")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null,
    ): List<XtreamSeriesDto>

    @GET("player_api.php?action=get_series_info")
    suspend fun getSeriesInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("series_id") seriesId: String,
    ): XtreamSeriesInfoDto

    // ---------- دليل البرامج (EPG) ----------
    @GET("player_api.php?action=get_short_epg")
    suspend fun getShortEpg(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("stream_id") streamId: String,
        @Query("limit") limit: Int = 4,
    ): XtreamEpgResponse

    // ---------- Catch-up ----------
    @GET("player_api.php?action=get_catchup_table")
    suspend fun getCatchupTable(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("stream_id") streamId: String,
    ): XtreamCatchupResponse
}
