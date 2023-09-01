package data.local.receiver

import domain.model.Receiver
import domain.states.DeletionState
import domain.states.InsertionState
import org.jetbrains.exposed.exceptions.ExposedSQLException
import javax.inject.Inject

class LocalReceiversDataSource @Inject constructor() : ReceiversDataSource {
    override suspend fun addReceiver(telegramChatId: Long): InsertionState {
        return try {
            ReceiversTable.insert(telegramChatId)
            InsertionState.Success
        } catch (e: ExposedSQLException) {
            InsertionState.Duplicate
        } catch (e: Exception) {
            e.printStackTrace()
            InsertionState.Error
        }
    }

    override suspend fun removeReceiver(telegramChatId: Long): DeletionState {
        return if (ReceiversTable.removeReceiver(telegramChatId)) {
            DeletionState.Success
        } else {
            DeletionState.Error
        }
    }

    override suspend fun getReceiversList(): List<Receiver> {
        try {
            return ReceiversTable.getAllReceivers()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return listOf()
    }
}