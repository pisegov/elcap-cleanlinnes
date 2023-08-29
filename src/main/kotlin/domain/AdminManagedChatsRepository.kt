package domain

import domain.model.Chat
import domain.states.DeletionState
import domain.states.InsertionState

interface AdminManagedChatsRepository {
    suspend fun addChat(telegramChatId: Long): InsertionState
    suspend fun removeChat(telegramChatId: Long): DeletionState

    suspend fun getChatsList(): List<Chat>
}