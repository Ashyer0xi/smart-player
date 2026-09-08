package com.iptv.smartplayer.presentation.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.domain.model.ContentCategory
import com.iptv.smartplayer.domain.model.Movie
import com.iptv.smartplayer.domain.usecase.GetMoviesUseCase
import com.iptv.smartplayer.domain.usecase.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ViewMode { GRID, LIST }
enum class SortOption(val label: String) {
    NEWEST("الأحدث"), TOP_RATED("الأعلى تقييماً"), ALPHABETICAL("أبجدي"),
}

data class MoviesUiState(
    val isLoading: Boolean = true,
    val movies: List<Movie> = emptyList(),
    val categories: List<ContentCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val sortOption: SortOption = SortOption.NEWEST,
    val viewMode: ViewMode = ViewMode.GRID,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val getMovies: GetMoviesUseCase,
    private val searchMovies: SearchMoviesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        loadMovies()
    }

    private fun loadMovies(categoryId: String? = null) {
        viewModelScope.launch {
            getMovies(categoryId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> _uiState.value.copy(isLoading = true)
                    is Resource.Success -> _uiState.value.copy(
                        isLoading = false,
                        movies = applySort(resource.data, _uiState.value.sortOption),
                        errorMessage = null,
                    )
                    is Resource.Error -> _uiState.value.copy(isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        loadMovies(categoryId)
    }

    fun onSortSelected(option: SortOption) {
        _uiState.value = _uiState.value.copy(
            sortOption = option,
            movies = applySort(_uiState.value.movies, option),
        )
    }

    fun onViewModeToggle() {
        val newMode = if (_uiState.value.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
        _uiState.value = _uiState.value.copy(viewMode = newMode)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.isBlank()) return
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300) // Debounce بسيط للبحث الفوري
            searchMovies(query).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(movies = resource.data)
                }
            }
        }
    }

    fun onSearchActiveChanged(active: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchActive = active)
        if (!active) loadMovies(_uiState.value.selectedCategoryId)
    }

    private fun applySort(movies: List<Movie>, option: SortOption): List<Movie> = when (option) {
        SortOption.NEWEST -> movies.sortedByDescending { it.releaseYear }
        SortOption.TOP_RATED -> movies.sortedByDescending { it.rating ?: 0.0 }
        SortOption.ALPHABETICAL -> movies.sortedBy { it.title }
    }
}
