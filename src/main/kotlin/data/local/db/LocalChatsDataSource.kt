package data.local.db

import data.local.ChatsDataSource
import domain.model.Chat
import javax.inject.Inject

class LocalChatsDataSource @Inject constructor() : ChatsDataSource {

    override suspend fun addChat(chat: Chat) {
        ChatsTable.insert(chat)
    }

    override suspend fun removeChat(telegramChatId: Long) {
        ChatsTable.removeChat(telegramChatId)
    }

    override suspend fun getChats(): List<Chat> {
        val dtoList: List<ChatDTO> = ChatsTable.getAllChats()
        return dtoList.map { it.toChat() }
    }
}