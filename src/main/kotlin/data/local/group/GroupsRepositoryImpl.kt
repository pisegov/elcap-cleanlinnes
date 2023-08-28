package data.local.group

import domain.GroupsRepository
import domain.model.Group
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GroupsRepositoryImpl @Inject constructor(private val dataSource: GroupsDataSource) : GroupsRepository {
    override suspend fun addGroup(group: Group) = withContext(Dispatchers.IO) {
        dataSource.addChat(group)
    }

    override suspend fun removeGroup(telegramChatId: Long) = withContext(Dispatchers.IO) {
        dataSource.removeGroup(telegramChatId)
    }

    override suspend fun getGroup(): List<Group> = withContext(Dispatchers.IO) {
        return@withContext dataSource.getChats()
    }
}