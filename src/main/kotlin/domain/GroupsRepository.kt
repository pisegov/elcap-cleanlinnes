package domain

import domain.model.Group

interface GroupsRepository {
    suspend fun addGroup(group: Group)
    suspend fun removeGroup(telegramChatId: Long)
    suspend fun getGroup(): List<Group>
}