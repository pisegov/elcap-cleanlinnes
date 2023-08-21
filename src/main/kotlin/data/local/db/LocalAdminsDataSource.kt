package data.local.db

import data.local.AdminsDataSource
import domain.model.Admin
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

    override suspend fun removeAdmin(telegramChatId: Long) {
        AdminsTable.removeAdmin(telegramChatId)
    }

    override suspend fun getAllAdmins(): List<Admin> {
        return AdminsTable.getAllAdmins()
    }
}