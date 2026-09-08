package com.iptv.smartplayer.domain.repository

import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.domain.model.ContentCategory
import com.iptv.smartplayer.domain.model.Season
import com.iptv.smartplayer.domain.model.Series
import kotlinx.coroutines.flow.Flow

interface SeriesRepository {
    fun getCategories(): Flow<Resource<List<ContentCategory>>>
    fun getSeriesList(categoryId: String? = null): Flow<Resource<List<Series>>>
    fun getSeriesDetails(seriesId: Int): Flow<Resource<Series>>
    fun getSeasons(seriesId: Int): Flow<Resource<List<Season>>>
}
