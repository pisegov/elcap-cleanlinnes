package data.local.admin

import domain.model.Admin
import domain.states.DeletionState
import domain.states.InsertionState
import org.jetbrains.exposed.exceptions.ExposedSQLException
import javax.inject.Inject

class LocalAdminsDataSource @Inject constructor() : AdminsDataSource {

    override suspend fun addAdmin(telegramChatId: Long): InsertionState {
        return try {
            AdminsTable.insert(telegramChatId)
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

    override suspend fun removeAdmin(telegramChatId: Long): DeletionState {
        return if (AdminsTable.removeAdmin(telegramChatId)) {
            DeletionState.Success
        } else {
            DeletionState.Error
        }
    }

    override suspend fun getAllAdmins(): List<Admin> {
        return AdminsTable.getAllAdmins()
    }
}