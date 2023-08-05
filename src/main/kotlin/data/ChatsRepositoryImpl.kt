package data

import data.local.ChatsDataSource
import domain.ChatsRepository
import domain.model.Chat

class ChatsRepositoryImpl(private val dataSource: ChatsDataSource) : ChatsRepository {
    override fun addChat(chat: Chat) {
        dataSource.addChat(chat)
    }

    override fun removeChat(telegramChatId: Long) {
        dataSource.removeChat(telegramChatId)
    }

    override fun getChats(): List<Chat> {
        return dataSource.getChats().map { it.value }
    }

}