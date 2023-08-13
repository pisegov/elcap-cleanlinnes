package domain

import domain.model.Chat

interface ChatsRepository {
    suspend fun addChat(chat: Chat)
    suspend fun removeChat(telegramChatId: Long)
    suspend fun getChats(): List<Chat>
}