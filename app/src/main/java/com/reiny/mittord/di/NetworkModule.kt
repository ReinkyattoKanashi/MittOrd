package com.reiny.mittord.di

import com.google.gson.Gson
import com.reiny.mittord.data.api.TranslateApiService
import com.reiny.mittord.data.repository.TranslateRepository
import com.reiny.mittord.data.repository.TranslateRepositoryImpl
import com.reiny.mittord.util.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(AppConstants.NETWORK_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        .readTimeout(AppConstants.NETWORK_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", AppConstants.TRANSLATE_USER_AGENT)
                .build()
            chain.proceed(request)
        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(AppConstants.TRANSLATE_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()

    @Provides
    @Singleton
    fun provideTranslateApiService(retrofit: Retrofit): TranslateApiService =
        retrofit.create(TranslateApiService::class.java)

    @Provides
    @Singleton
    fun provideTranslateRepository(apiService: TranslateApiService): TranslateRepository =
        TranslateRepositoryImpl(apiService)
}
