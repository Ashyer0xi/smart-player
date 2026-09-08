package com.iptv.smartplayer.presentation.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.domain.model.Channel
import com.iptv.smartplayer.domain.model.ContentCategory
import com.iptv.smartplayer.domain.model.EpgProgram
import com.iptv.smartplayer.domain.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveTvUiState(
    val isLoading: Boolean = true,
    val categories: List<ContentCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val channels: List<Channel> = emptyList(),
    val selectedChannel: Channel? = null,
    val epgForSelected: List<EpgProgram> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val repository: ChannelRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveTvUiState())
    val uiState: StateFlow<LiveTvUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadChannels()
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

    private fun loadChannels(categoryId: String? = null) {
        viewModelScope.launch {
            repository.getChannels(categoryId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> _uiState.value.copy(isLoading = true)
                    is Resource.Success -> _uiState.value.copy(
                        isLoading = false,
                        channels = resource.data,
                        selectedChannel = _uiState.value.selectedChannel ?: resource.data.firstOrNull(),
                        errorMessage = null,
                    ).also { it.selectedChannel?.let { ch -> loadEpg(ch.streamId) } }
                    is Resource.Error -> _uiState.value.copy(isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        loadChannels(categoryId)
    }

    fun onChannelSelected(channel: Channel) {
        _uiState.value = _uiState.value.copy(selectedChannel = channel)
        loadEpg(channel.streamId)
    }

    private fun loadEpg(streamId: Int) {
        viewModelScope.launch {
            repository.getEpgFor(streamId).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.value = _uiState.value.copy(epgForSelected = resource.data)
                }
            }
        }
    }
}
