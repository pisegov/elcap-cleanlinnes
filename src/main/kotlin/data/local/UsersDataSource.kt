package data.local

import domain.model.User

interface UsersDataSource {
    suspend fun addUser(newUser: User)
    suspend fun removeUser(telegramChatId: Long)

    suspend fun getUsers(): List<User>
}