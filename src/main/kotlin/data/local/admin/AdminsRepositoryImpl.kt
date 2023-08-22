package data.local.admin

import domain.AdminsRepository
import domain.model.Admin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AdminsRepositoryImpl @Inject constructor(private val dataSource: AdminsDataSource) : AdminsRepository {
    override suspend fun addAdmin(telegramChatId: Long) = withContext(Dispatchers.IO) {
        return@withContext dataSource.addAdmin(telegramChatId)
    }

    override suspend fun removeAdmin(telegramChatId: Long) = withContext(Dispatchers.IO) {
        dataSource.removeAdmin(telegramChatId)
    }

    override suspend fun getAdminsList(): List<Admin> = withContext(Dispatchers.IO) {
        return@withContext dataSource.getAllAdmins()
    }
}