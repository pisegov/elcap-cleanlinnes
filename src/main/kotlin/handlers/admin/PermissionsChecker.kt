package handlers.admin

import domain.AdminsRepository
import javax.inject.Inject

class PermissionsChecker @Inject constructor(private val adminsRepository: AdminsRepository) {
    suspend fun checkPermissions(telegramChatId: Long): Boolean {
        return adminsRepository
            .getAdminsList()
            .map { it.telegramChatId }
            .contains(telegramChatId)
    }
}