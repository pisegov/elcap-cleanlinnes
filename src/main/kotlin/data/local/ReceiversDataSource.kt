package data.local

import domain.model.Receiver

interface ReceiversDataSource {
    fun addReceiver(telegramChatId: Long)
    fun removeReceiver(telegramChatId: Long)

    fun getReceiversList(): List<Receiver>
}