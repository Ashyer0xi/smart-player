package com.iptv.smartplayer.presentation.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.domain.model.Episode
import com.iptv.smartplayer.domain.model.Season
import com.iptv.smartplayer.domain.model.Series
import com.iptv.smartplayer.domain.repository.SeriesRepository
import com.iptv.smartplayer.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeriesDetailsUiState(
    val isLoading: Boolean = true,
    val series: Series? = null,
    val seasons: List<Season> = emptyList(),
    val selectedSeasonNumber: Int = 1,
    val errorMessage: String? = null,
) {
    val currentEpisodes: List<Episode>
        get() = seasons.firstOrNull { it.seasonNumber == selectedSeasonNumber }?.episodes ?: emptyList()
}

@HiltViewModel
class SeriesDetailsViewModel @Inject constructor(
    private val repository: SeriesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val seriesId: Int = checkNotNull(savedStateHandle[Screen.SeriesDetails.ARG_SERIES_ID])

    private val _uiState = MutableStateFlow(SeriesDetailsUiState())
    val uiState: StateFlow<SeriesDetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
        loadSeasons()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            repository.getSeriesDetails(seriesId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(series = resource.data)
                }
            }
        }
    }

    private fun loadSeasons() {
        viewModelScope.launch {
            repository.getSeasons(seriesId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> _uiState.value.copy(isLoading = true)
                    is Resource.Success -> _uiState.value.copy(
                        isLoading = false,
                        seasons = resource.data,
                        selectedSeasonNumber = resource.data.firstOrNull()?.seasonNumber ?: 1,
                        errorMessage = null,
                    )
                    is Resource.Error -> _uiState.value.copy(isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    fun onSeasonSelected(seasonNumber: Int) {
        _uiState.value = _uiState.value.copy(selectedSeasonNumber = seasonNumber)
    }
}
