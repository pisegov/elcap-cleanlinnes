package domain

import domain.model.Chat

interface ChatsRepository {
    fun addChat(chat: Chat)
    fun removeChat(telegramChatId: Long)
    fun getChats(): List<Chat>
}