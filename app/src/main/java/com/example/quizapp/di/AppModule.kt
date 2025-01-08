package com.example.quizapp.di

import com.example.quizapp.data.local.SharedPreferencesTokenStorage
import com.example.quizapp.data.local.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    fun provideTokenStorage(
        tokenStorage: SharedPreferencesTokenStorage,
    ): TokenStorage = tokenStorage
}
