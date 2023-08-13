package data.local

import domain.model.Receiver

interface ReceiversDataSource {
    suspend fun addReceiver(telegramChatId: Long)
    suspend fun removeReceiver(telegramChatId: Long)

    suspend fun getReceiversList(): List<Receiver>
}