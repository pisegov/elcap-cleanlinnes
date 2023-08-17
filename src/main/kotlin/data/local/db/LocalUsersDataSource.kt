package data.local.db

import data.local.UsersDataSource
import domain.model.User
import javax.inject.Inject

class LocalUsersDataSource @Inject constructor() : UsersDataSource {
    override suspend fun addUser(newUser: User) {
        try {
            UsersTable.insert(newUser)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override suspend fun removeUser(telegramChatId: Long) {
        UsersTable.removeUser(telegramChatId)
    }

    override suspend fun getUsers(): List<User> {
        return UsersTable.getAllUsers().map { it.toUser() }
    }
}