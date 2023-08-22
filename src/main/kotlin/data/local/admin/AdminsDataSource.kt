package data.local.admin

import domain.model.Admin
import domain.states.InsertionState

interface AdminsDataSource {
    suspend fun addAdmin(telegramChatId: Long): InsertionState
    suspend fun removeAdmin(telegramChatId: Long)

    suspend fun getAllAdmins(): List<Admin>
}