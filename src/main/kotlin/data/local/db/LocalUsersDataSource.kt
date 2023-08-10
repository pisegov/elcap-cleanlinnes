package data.local.db

import data.local.UsersDataSource
import domain.model.User
import javax.inject.Inject

class LocalUsersDataSource @Inject constructor() : UsersDataSource {
    override fun addUser(newUser: User) {
        UsersTable.insert(newUser)
    }

    override fun removeUser(telegramChatId: Long) {
        UsersTable.removeUser(telegramChatId)
    }

    override fun getUsers(): List<User> {
        return UsersTable.getAllUsers().map { it.toUser() }
    }
}