package domain

import domain.model.Receiver

interface ReceiversRepository {
    suspend fun addReceiver(telegramChatId: Long)
    suspend fun removeReceiver(telegramChatId: Long)

    suspend fun getReceiversList(): List<Receiver>
}