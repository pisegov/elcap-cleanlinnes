package data.local.db

import data.local.ReceiversDataSource
import domain.model.Receiver
import javax.inject.Inject

class LocalReceiversDataSource @Inject constructor() : ReceiversDataSource {
    override suspend fun addReceiver(telegramChatId: Long) {
        ReceiversTable.insert(telegramChatId)
    }

    override suspend fun removeReceiver(telegramChatId: Long) {
        ReceiversTable.removeReceiver(telegramChatId)
    }

    override suspend fun getReceiversList(): List<Receiver> {
        return ReceiversTable.getAllReceivers()
    }
}