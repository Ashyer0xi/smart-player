package com.iptv.smartplayer.domain.repository

import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.domain.model.Channel
import com.iptv.smartplayer.domain.model.ContentCategory
import com.iptv.smartplayer.domain.model.EpgProgram
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    fun getCategories(): Flow<Resource<List<ContentCategory>>>
    fun getChannels(categoryId: String? = null): Flow<Resource<List<Channel>>>
    fun getEpgFor(streamId: Int): Flow<Resource<List<EpgProgram>>>
}
