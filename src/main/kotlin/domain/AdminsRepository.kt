package domain

import domain.model.Admin

interface AdminsRepository {
    fun addAdmin(telegramChatId: Long)
    fun removeAdmin(telegramChatId: Long)

    fun getAdminsList(): List<Admin>
}