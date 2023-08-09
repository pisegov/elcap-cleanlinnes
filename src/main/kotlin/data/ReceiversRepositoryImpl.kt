package data

import data.local.ReceiversDataSource
import domain.ReceiversRepository
import domain.model.Receiver
import javax.inject.Inject

class ReceiversRepositoryImpl @Inject constructor(private val dataSource: ReceiversDataSource) : ReceiversRepository {

    override fun addReceiver(telegramChatId: Long) {
        dataSource.addReceiver(telegramChatId)
    }

    override fun removeReceiver(telegramChatId: Long) {
        dataSource.removeReceiver(telegramChatId)
    }

    override fun getReceiversList(): List<Receiver> {
        return dataSource.getReceiversList()
    }
}