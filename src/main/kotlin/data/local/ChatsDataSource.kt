package data.local

import domain.model.Chat

interface ChatsDataSource {
    fun addChat(chat: Chat)
    fun removeChat(telegramChatId: Long)

    fun getChats(): List<Chat>
}