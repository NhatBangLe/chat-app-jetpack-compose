package com.application.android.chatapplication.di

import com.application.android.chatapplication.data.datasource.IChatService
import com.application.android.chatapplication.data.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideChatRepository(chatService: IChatService) : ChatRepository {
        return ChatRepository(chatService)
    }

}