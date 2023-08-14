package domain

import domain.model.Receiver
import domain.states.InsertionState

interface ReceiversRepository {
    suspend fun addReceiver(telegramChatId: Long): InsertionState
    suspend fun removeReceiver(telegramChatId: Long)

    suspend fun getReceiversList(): List<Receiver>
}