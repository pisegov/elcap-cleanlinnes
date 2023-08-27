package handlers.admin

import domain.AdminsRepository
import javax.inject.Inject

class PermissionsChecker @Inject constructor(private val adminsRepository: AdminsRepository) {
    suspend fun <T> checkPermissions(telegramChatId: Long, block: suspend () -> T): T {
        if (adminsRepository.getAdminsList().map { it.telegramChatId }.contains(telegramChatId)) {
            return block.invoke()
        } else
            throw Exception("Permission denied")
    }
}