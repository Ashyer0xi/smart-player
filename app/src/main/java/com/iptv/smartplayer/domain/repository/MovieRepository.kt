package com.iptv.smartplayer.domain.repository

import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.domain.model.ContentCategory
import com.iptv.smartplayer.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getCategories(): Flow<Resource<List<ContentCategory>>>
    fun getMovies(categoryId: String? = null): Flow<Resource<List<Movie>>>
    fun getMovieDetails(streamId: Int): Flow<Resource<Movie>>
    fun searchMovies(query: String): Flow<Resource<List<Movie>>>
    fun getTrendingMovies(): Flow<Resource<List<Movie>>>
    fun getSimilarMovies(tmdbId: Int): Flow<Resource<List<Movie>>>
}
