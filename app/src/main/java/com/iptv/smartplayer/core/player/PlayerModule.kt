package com.iptv.smartplayer.core.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.database.DefaultDatabaseProvider
import androidx.media3.exoplayer.upstream.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.exoplayer.upstream.cache.SimpleCache
import androidx.media3.exoplayer.upstream.DefaultDataSource
import androidx.media3.exoplayer.upstream.cache.CacheDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@UnstableApi
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): SimpleCache {
        val cacheDir = File(context.cacheDir, "media_cache")
        val maxCacheBytes = 200L * 1024L * 1024L // 200 MB, قابل التعديل لاحقاً
        val evictor = LeastRecentlyUsedCacheEvictor(maxCacheBytes)
        val databaseProvider = DefaultDatabaseProvider(context)
        return SimpleCache(cacheDir, evictor, databaseProvider)
    }

    @Provides
    @Singleton
    fun provideCacheDataSourceFactory(@ApplicationContext context: Context, cache: SimpleCache): CacheDataSource.Factory {
        val upstreamFactory = DefaultDataSource.Factory(context)
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context, cacheDataSourceFactory: CacheDataSource.Factory): ExoPlayer {
        // إعدادات مخزن مؤقت مضبوطة للبث المباشر — تُوازن بين زمن البدء واستقرار التشغيل
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 15_000,
                /* maxBufferMs = */ 50_000,
                /* bufferForPlaybackMs = */ 2_500,
                /* bufferForPlaybackAfterRebufferMs = */ 5_000,
            )
            .build()

        return ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            // فك تشفير عتادي مفضّل تلقائياً من ExoPlayer عند توفره على الجهاز
            .build()
    }
}
