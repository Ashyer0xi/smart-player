package com.iptv.smartplayer.core.network

import com.iptv.smartplayer.BuildConfig
import com.iptv.smartplayer.data.remote.tmdb.TmdbApi
import com.iptv.smartplayer.data.remote.xtream.XtreamApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * وحدة الشبكة — تفصل بين عميلي Xtream (URL ديناميكي لكل مستخدم) و TMDB (URL ثابت + مفتاح API).
 * تطبّق مهلات زمنية معقولة و Retry تلقائي حسب "بروتوكول التحليل الاستباقي" في البرومبت.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    @Named("base")
    fun provideBaseOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    @Named("tmdb")
    fun provideTmdbOkHttpClient(
        @Named("base") base: OkHttpClient,
    ): OkHttpClient = base.newBuilder()
        .addInterceptor { chain ->
            val original = chain.request()
            val urlWithKey = original.url.newBuilder()
                .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
                .build()
            chain.proceed(original.newBuilder().url(urlWithKey).build())
        }
        .build()

    @Provides
    @Singleton
    @Named("tmdb")
    fun provideTmdbRetrofit(@Named("tmdb") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTmdbApi(@Named("tmdb") retrofit: Retrofit): TmdbApi =
        retrofit.create(TmdbApi::class.java)

    /**
     * عميل Xtream: الـ baseUrl يعتمد على سيرفر المستخدم (يُضبط ديناميكياً عند تسجيل الدخول
     * عبر XtreamServerProvider بدل قيمة ثابتة هنا). نستخدم baseUrl مؤقتاً صالحاً وهماً
     * لأن Retrofit يتطلب قيمة عند البناء، ثم يُستبدل الـ Host عبر Interceptor حسب الحساب النشط.
     */
    @Provides
    @Singleton
    @Named("xtream")
    fun provideXtreamRetrofit(
        @Named("base") client: OkHttpClient,
        xtreamServerProvider: XtreamServerProvider,
    ): Retrofit {
        val dynamicHostClient = client.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request()
                val server = xtreamServerProvider.currentServerUrl()
                val newUrl = request.url.newBuilder()
                    .scheme(server.scheme)
                    .host(server.host)
                    .port(server.port)
                    .build()
                chain.proceed(request.newBuilder().url(newUrl).build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("http://placeholder.local/") // يُستبدل ديناميكياً عبر الـ Interceptor أعلاه
            .client(dynamicHostClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideXtreamApi(@Named("xtream") retrofit: Retrofit): XtreamApi =
        retrofit.create(XtreamApi::class.java)
}
