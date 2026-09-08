package com.iptv.smartplayer.core.di

import com.iptv.smartplayer.data.repository.ChannelRepositoryImpl
import com.iptv.smartplayer.data.repository.LibraryRepositoryImpl
import com.iptv.smartplayer.data.repository.MovieRepositoryImpl
import com.iptv.smartplayer.data.repository.SeriesRepositoryImpl
import com.iptv.smartplayer.domain.repository.ChannelRepository
import com.iptv.smartplayer.domain.repository.LibraryRepository
import com.iptv.smartplayer.domain.repository.MovieRepository
import com.iptv.smartplayer.domain.repository.SeriesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * يربط كل واجهة Repository في طبقة Domain بتطبيقها الفعلي في طبقة Data.
 * الشاشات وViewModels تعتمد فقط على الواجهات (Domain) وليس هذا الملف مباشرة —
 * هذا ما يحافظ على استقلالية طبقة العرض عن تفاصيل Xtream/TMDB/Room.
 *
 * TODO: إضافة ربط SeriesRepository، ChannelRepository، FavoriteRepository،
 * WatchHistoryRepository، CustomCatalogRepository بنفس النمط عند بناء كل منها
 * في المراحل التالية (العقل الرابع والسادس).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindSeriesRepository(impl: SeriesRepositoryImpl): SeriesRepository

    @Binds
    @Singleton
    abstract fun bindChannelRepository(impl: ChannelRepositoryImpl): ChannelRepository
}
