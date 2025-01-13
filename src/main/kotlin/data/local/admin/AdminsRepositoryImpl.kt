package data.local.admin

import domain.AdminsRepository
import domain.model.Admin
import domain.model.Chat
import domain.states.DeletionState
import domain.states.InsertionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AdminsRepositoryImpl @Inject constructor(private val dataSource: AdminsDataSource) : AdminsRepository {
    override suspend fun addAdmin(telegramChatId: Long) = withContext(Dispatchers.IO) {
        dataSource.addAdmin(telegramChatId)
    }

    override suspend fun removeAdmin(telegramChatId: Long) = withContext(Dispatchers.IO) {
        dataSource.removeAdmin(telegramChatId)
    }

    override suspend fun getAdminsList(): List<Admin> = withContext(Dispatchers.IO) {
        dataSource.getAllAdmins()
    }

    override suspend fun addChat(telegramChatId: Long): InsertionState = addAdmin(telegramChatId)
    override suspend fun removeChat(telegramChatId: Long): DeletionState = removeAdmin(telegramChatId)
    override suspend fun getChatsList(): List<Chat> = getAdminsList()
}