package com.iptv.smartplayer.data.repository

import android.util.Base64
import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.data.remote.xtream.XtreamApi
import com.iptv.smartplayer.domain.model.Channel
import com.iptv.smartplayer.domain.model.ContentCategory
import com.iptv.smartplayer.domain.model.EpgProgram
import com.iptv.smartplayer.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ChannelRepositoryImpl @Inject constructor(
    private val xtreamApi: XtreamApi,
    private val credentialsProvider: XtreamCredentialsProvider,
) : ChannelRepository {

    override fun getCategories(): Flow<Resource<List<ContentCategory>>> = flow {
        emit(Resource.Loading())
        try {
            val creds = credentialsProvider.current()
            val categories = xtreamApi.getLiveCategories(creds.username, creds.password)
                .map { ContentCategory(id = it.categoryId, name = it.categoryName) }
            emit(Resource.Success(categories))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب تصنيفات القنوات"))
        }
    }

    override fun getChannels(categoryId: String?): Flow<Resource<List<Channel>>> = flow {
        emit(Resource.Loading())
        try {
            val creds = credentialsProvider.current()
            val channels = xtreamApi.getLiveStreams(creds.username, creds.password, categoryId).map { dto ->
                Channel(
                    streamId = dto.streamId,
                    name = dto.name,
                    logoUrl = dto.streamIcon,
                    categoryId = dto.categoryId,
                    supportsCatchup = dto.tvArchive == 1,
                )
            }
            emit(Resource.Success(channels))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب القنوات"))
        }
    }

    override fun getEpgFor(streamId: Int): Flow<Resource<List<EpgProgram>>> = flow {
        emit(Resource.Loading())
        try {
            val creds = credentialsProvider.current()
            val response = xtreamApi.getShortEpg(creds.username, creds.password, streamId.toString())
            val programs = response.listings?.map { item ->
                EpgProgram(
                    title = decodeBase64(item.title),
                    description = decodeBase64(item.description),
                    startEpochMillis = item.start?.let { parseXtreamDate(it) } ?: 0L,
                    endEpochMillis = item.end?.let { parseXtreamDate(it) } ?: 0L,
                )
            } ?: emptyList()
            emit(Resource.Success(programs))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "تعذر جلب دليل البرامج"))
        }
    }

    // عناوين وأوصاف EPG في Xtream Codes تُرسَل مُرمَّزة بـ Base64
    private fun decodeBase64(value: String?): String =
        try {
            value?.let { String(Base64.decode(it, Base64.DEFAULT)) } ?: ""
        } catch (e: Exception) {
            value ?: ""
        }

    // صيغة تاريخ Xtream الشائعة: "yyyy-MM-dd HH:mm:ss"
    private fun parseXtreamDate(raw: String): Long =
        try {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).parse(raw)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
}
