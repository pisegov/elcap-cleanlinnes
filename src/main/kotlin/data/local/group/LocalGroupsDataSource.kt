package data.local.group

import data.local.toGroup
import domain.model.Group
import javax.inject.Inject

class LocalGroupsDataSource @Inject constructor() : GroupsDataSource {

    override suspend fun addChat(group: Group) {
        GroupsTable.insert(group)
    }

    override suspend fun removeGroup(telegramChatId: Long) {
        GroupsTable.removeGroup(telegramChatId)
    }

    override suspend fun getChats(): List<Group> {
        val dtoList: List<GroupDTO> = GroupsTable.getAllGroups()
        return dtoList.map { it.toGroup() }
    }
}