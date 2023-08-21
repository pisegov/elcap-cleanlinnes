package domain

import domain.model.Admin
import domain.states.InsertionState

interface AdminsRepository {
    suspend fun addAdmin(telegramChatId: Long): InsertionState
    suspend fun removeAdmin(telegramChatId: Long)

    suspend fun getAdminsList(): List<Admin>
}