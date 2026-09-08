package com.iptv.smartplayer.presentation.home

import com.iptv.smartplayer.data.local.entity.WatchHistoryEntity
import com.iptv.smartplayer.domain.model.Movie

/** عنصر واحد يظهر في الـHero المتحرك أعلى الشاشة الرئيسية */
data class HeroItem(
    val contentId: String,
    val title: String,
    val description: String,
    val backdropUrl: String?,
    val logoUrl: String? = null,
    val year: String?,
    val isHd: Boolean = true,
)

/** صف أفقي عام من المحتوى — يُستخدم لكل صفوف الرئيسية (رائج، مقترح، أُضيف حديثاً...) */
data class ContentRow(
    val title: String,
    val movies: List<Movie>,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val heroItems: List<HeroItem> = emptyList(),
    val continueWatching: List<WatchHistoryEntity> = emptyList(),
    val trending: ContentRow? = null,
    val recentlyAdded: ContentRow? = null,
    val genreRows: List<ContentRow> = emptyList(),
    val errorMessage: String? = null,
)
