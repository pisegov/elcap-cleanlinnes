package data.local.db

import data.local.ReceiversDataSource
import domain.model.Receiver
import domain.states.InsertionState
import org.jetbrains.exposed.exceptions.ExposedSQLException
import javax.inject.Inject

class LocalReceiversDataSource @Inject constructor() : ReceiversDataSource {
    override suspend fun addReceiver(telegramChatId: Long): InsertionState {
        return try {
            ReceiversTable.insert(telegramChatId)
            InsertionState.Success
        } catch (e: ExposedSQLException) {
            e.printStackTrace()
            println(e.contexts)

            InsertionState.Duplicate
        } catch (e: Exception) {
            e.printStackTrace()
            InsertionState.Error
        }
    }

    override suspend fun removeReceiver(telegramChatId: Long) {
        ReceiversTable.removeReceiver(telegramChatId)
    }

    override suspend fun getReceiversList(): List<Receiver> {
        return ReceiversTable.getAllReceivers()
    }
}