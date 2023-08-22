package ioc

import dagger.Binds
import dagger.Module
import data.local.chat.ChatsDataSource
import data.local.chat.ChatsRepositoryImpl
import data.local.chat.LocalChatsDataSource
import domain.ChatsRepository

@Module
interface ChatsModule {
    @Binds
    fun chatsDataSource(impl: LocalChatsDataSource): ChatsDataSource

    @Binds
    fun chatsRepository(impl: ChatsRepositoryImpl): ChatsRepository
}