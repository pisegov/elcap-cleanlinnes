package data.local.group

import domain.model.Group

interface GroupsDataSource {
    suspend fun addChat(group: Group)
    suspend fun removeGroup(telegramChatId: Long)

    suspend fun getChats(): List<Group>
}