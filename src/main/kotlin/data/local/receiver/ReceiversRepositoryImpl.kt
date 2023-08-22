package data.local.receiver

import domain.ReceiversRepository
import domain.model.Receiver
import domain.states.InsertionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReceiversRepositoryImpl @Inject constructor(private val dataSource: ReceiversDataSource) : ReceiversRepository {
    override suspend fun addReceiver(telegramChatId: Long): InsertionState = withContext(Dispatchers.IO) {
        return@withContext dataSource.addReceiver(telegramChatId)
    }

    override suspend fun removeReceiver(telegramChatId: Long) = withContext(Dispatchers.IO) {
        dataSource.removeReceiver(telegramChatId)
    }

    override suspend fun getReceiversList(): List<Receiver> = withContext(Dispatchers.IO) {
        return@withContext dataSource.getReceiversList()
    }
}