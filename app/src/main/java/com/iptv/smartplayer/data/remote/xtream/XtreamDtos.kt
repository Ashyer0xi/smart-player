package com.iptv.smartplayer.data.remote.xtream

import com.google.gson.annotations.SerializedName

// ==================== المصادقة ====================
data class XtreamAuthResponse(
    @SerializedName("user_info") val userInfo: XtreamUserInfo?,
    @SerializedName("server_info") val serverInfo: XtreamServerInfo?,
)

data class XtreamUserInfo(
    @SerializedName("username") val username: String?,
    @SerializedName("status") val status: String?, // "Active" / "Expired" / "Banned"
    @SerializedName("exp_date") val expDate: String?, // Unix timestamp كنص
    @SerializedName("is_trial") val isTrial: String?,
    @SerializedName("active_cons") val activeConnections: String?,
    @SerializedName("max_connections") val maxConnections: String?,
)

data class XtreamServerInfo(
    @SerializedName("url") val url: String?,
    @SerializedName("port") val port: String?,
    @SerializedName("https_port") val httpsPort: String?,
    @SerializedName("server_protocol") val protocol: String?,
    @SerializedName("timezone") val timezone: String?,
)

// ==================== التصنيفات (مشتركة بين Live/VOD/Series) ====================
data class XtreamCategoryDto(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int? = 0,
)

// ==================== القنوات المباشرة ====================
data class XtreamLiveStreamDto(
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("epg_channel_id") val epgChannelId: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("num") val num: Int?,
    // 1 إذا كانت القناة تدعم Catch-up
    @SerializedName("tv_archive") val tvArchive: Int? = 0,
)

// ==================== الأفلام (VOD) ====================
data class XtreamVodStreamDto(
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("container_extension") val containerExtension: String?,
)

data class XtreamVodInfoDto(
    @SerializedName("info") val info: XtreamVodDetailsDto?,
    @SerializedName("movie_data") val movieData: XtreamVodStreamDto?,
)

data class XtreamVodDetailsDto(
    @SerializedName("tmdb_id") val tmdbId: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("releasedate") val releaseDate: String?,
)

// ==================== المسلسلات ====================
data class XtreamSeriesDto(
    @SerializedName("series_id") val seriesId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("cover") val cover: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("last_modified") val lastModified: String?,
)

data class XtreamSeriesInfoDto(
    @SerializedName("info") val info: XtreamSeriesDto?,
    // خريطة: رقم الموسم -> قائمة الحلقات
    @SerializedName("episodes") val episodes: Map<String, List<XtreamEpisodeDto>>?,
)

data class XtreamEpisodeDto(
    @SerializedName("id") val id: String,
    @SerializedName("episode_num") val episodeNum: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("container_extension") val containerExtension: String?,
    @SerializedName("season") val season: Int,
    @SerializedName("info") val info: XtreamEpisodeInfoDto? = null,
)

data class XtreamEpisodeInfoDto(
    @SerializedName("duration") val duration: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("movie_image") val thumbnail: String?,
)

// ==================== EPG ====================
data class XtreamEpgResponse(
    @SerializedName("epg_listings") val listings: List<XtreamEpgItemDto>?,
)

data class XtreamEpgItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String?, // Base64-encoded من Xtream
    @SerializedName("description") val description: String?, // Base64-encoded
    @SerializedName("start") val start: String?,
    @SerializedName("end") val end: String?,
)

// ==================== Catch-up ====================
data class XtreamCatchupResponse(
    @SerializedName("epg_listings") val listings: List<XtreamEpgItemDto>?,
)
