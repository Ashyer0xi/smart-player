package com.iptv.smartplayer.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            // Migrations الحقيقية تُضاف بدل fallbackToDestructiveMigration قبل الإطلاق الفعلي
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFavoriteDao(db: AppDatabase) = db.favoriteDao()

    @Provides
    fun provideWatchHistoryDao(db: AppDatabase) = db.watchHistoryDao()

    @Provides
    fun provideCustomCatalogDao(db: AppDatabase) = db.customCatalogDao()

    @Provides
    fun provideChannelDao(db: AppDatabase) = db.channelDao()

    @Provides
    fun provideEpgDao(db: AppDatabase) = db.epgDao()

    @Provides
    fun provideTmdbCacheDao(db: AppDatabase) = db.tmdbCacheDao()
}
