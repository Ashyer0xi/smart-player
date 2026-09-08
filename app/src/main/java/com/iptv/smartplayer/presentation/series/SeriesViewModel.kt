package com.iptv.smartplayer.presentation.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.domain.model.ContentCategory
import com.iptv.smartplayer.domain.model.Series
import com.iptv.smartplayer.domain.repository.SeriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeriesListUiState(
    val isLoading: Boolean = true,
    val series: List<Series> = emptyList(),
    val categories: List<ContentCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val repository: SeriesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesListUiState())
    val uiState: StateFlow<SeriesListUiState> = _uiState.asStateFlow()

    init {
        loadSeries()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories().collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(categories = resource.data)
                }
            }
        }
    }

    private fun loadSeries(categoryId: String? = null) {
        viewModelScope.launch {
            repository.getSeriesList(categoryId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> _uiState.value.copy(isLoading = true)
                    is Resource.Success -> _uiState.value.copy(isLoading = false, series = resource.data, errorMessage = null)
                    is Resource.Error -> _uiState.value.copy(isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        loadSeries(categoryId)
    }
}
