package com.iptv.smartplayer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.domain.repository.LibraryRepository
import com.iptv.smartplayer.domain.usecase.GetTrendingMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrendingMovies: GetTrendingMoviesUseCase,
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    private fun loadHome() {
        viewModelScope.launch {
            combine(
                getTrendingMovies(),
                libraryRepository.observeContinueWatching(),
            ) { trendingResource, continueWatching ->
                when (trendingResource) {
                    is Resource.Loading -> _uiState.value.copy(isLoading = true)
                    is Resource.Success -> {
                        val movies = trendingResource.data
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            heroItems = movies.take(5).map {
                                HeroItem(
                                    contentId = it.xtreamStreamId.toString(),
                                    title = it.title,
                                    description = it.overview.orEmpty(),
                                    backdropUrl = it.backdropUrl,
                                    year = it.releaseYear,
                                )
                            },
                            continueWatching = continueWatching,
                            trending = ContentRow(title = "الأكثر رواجاً هذا الأسبوع", movies = movies),
                        )
                    }
                    is Resource.Error -> _uiState.value.copy(
                        isLoading = false,
                        errorMessage = trendingResource.message,
                        continueWatching = continueWatching,
                    )
                }
            }.collect { newState -> _uiState.value = newState }
        }
    }

    fun retry() = loadHome()
}
