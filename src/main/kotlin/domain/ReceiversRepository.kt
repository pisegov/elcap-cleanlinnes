package domain

import domain.model.Receiver
import domain.states.DeletionState
import domain.states.InsertionState

interface ReceiversRepository : AdminManagedChatsRepository {
    suspend fun addReceiver(telegramChatId: Long): InsertionState
    suspend fun removeReceiver(telegramChatId: Long): DeletionState

    suspend fun getReceiversList(): List<Receiver>
}