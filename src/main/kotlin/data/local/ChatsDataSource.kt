package data.local

import domain.model.Chat

interface ChatsDataSource {
    fun getChatById(telegramChatId: Long): Chat?
    fun addChat(chat: Chat)
    fun removeChat(telegramChatId: Long)

    fun getChats(): Map<Long, Chat>
}