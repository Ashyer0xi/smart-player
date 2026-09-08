package com.iptv.smartplayer.presentation.movies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.data.local.entity.ContentType
import com.iptv.smartplayer.domain.model.Movie
import com.iptv.smartplayer.domain.repository.LibraryRepository
import com.iptv.smartplayer.domain.repository.MovieRepository
import com.iptv.smartplayer.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieDetailsUiState(
    val isLoading: Boolean = true,
    val movie: Movie? = null,
    val similar: List<Movie> = emptyList(),
    val isFavorite: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val libraryRepository: LibraryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle[Screen.MovieDetails.ARG_MOVIE_ID])

    private val _uiState = MutableStateFlow(MovieDetailsUiState())
    val uiState: StateFlow<MovieDetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
        observeFavoriteState()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            movieRepository.getMovieDetails(movieId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> _uiState.value.copy(isLoading = true)
                    is Resource.Success -> {
                        resource.data.tmdbId?.let { loadSimilar(it) }
                        _uiState.value.copy(isLoading = false, movie = resource.data, errorMessage = null)
                    }
                    is Resource.Error -> _uiState.value.copy(isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    private fun loadSimilar(tmdbId: Int) {
        viewModelScope.launch {
            movieRepository.getSimilarMovies(tmdbId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(similar = resource.data)
                }
            }
        }
    }

    private fun observeFavoriteState() {
        viewModelScope.launch {
            libraryRepository.observeIsFavorite(movieId.toString()).collect { isFavorite ->
                _uiState.value = _uiState.value.copy(isFavorite = isFavorite)
            }
        }
    }

    fun toggleFavorite() {
        val movie = _uiState.value.movie ?: return
        viewModelScope.launch {
            libraryRepository.toggleFavorite(
                contentId = movieId.toString(),
                contentType = ContentType.MOVIE,
                title = movie.title,
                posterUrl = movie.posterUrl,
            )
        }
    }
}
