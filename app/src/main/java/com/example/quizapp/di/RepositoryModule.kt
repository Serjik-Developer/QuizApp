package com.example.quizapp.di

import com.example.quizapp.data.repository.AdminRepositoryImpl
import com.example.quizapp.data.repository.AuthRepositoryImpl
import com.example.quizapp.data.repository.QuizRepositoryImpl
import com.example.quizapp.domain.repository.AdminRepository
import com.example.quizapp.domain.repository.AuthRepository
import com.example.quizapp.domain.repository.QuizRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        repository: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        repository: QuizRepositoryImpl,
    ): QuizRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        repository: AdminRepositoryImpl,
    ): AdminRepository
}
