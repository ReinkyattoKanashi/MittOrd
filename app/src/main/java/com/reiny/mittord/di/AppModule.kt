package com.reiny.mittord.di

import android.content.Context
import com.reiny.mittord.core.RunAsync
import com.reiny.mittord.database.CacheModule
import com.reiny.mittord.database.DictionaryRepository
import com.reiny.mittord.database.DictionaryRepositoryImpl
import com.reiny.mittord.database.dao.SemanticObjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCacheModule(@ApplicationContext context: Context): CacheModule =
        CacheModule.Base(context)

    @Provides
    fun provideDao(cacheModule: CacheModule): SemanticObjectDao =
        cacheModule.dao()

    @Provides
    @Singleton
    fun provideRepository(dao: SemanticObjectDao): DictionaryRepository =
        DictionaryRepositoryImpl(dao)

    @Provides
    fun provideRunAsync(): RunAsync = RunAsync.Base()
}