package data.local.chat

import domain.model.Chat

interface ChatsDataSource {
    suspend fun addChat(chat: Chat)
    suspend fun removeChat(telegramChatId: Long)

    suspend fun getChats(): List<Chat>
}