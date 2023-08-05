package domain

import domain.model.Receiver

interface ReceiversRepository {
    fun addReceiver(telegramChatId: Long)
    fun removeReceiver(telegramChatId: Long)

    fun getReceiversList(): List<Receiver>
}