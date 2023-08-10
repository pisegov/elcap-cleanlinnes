package data.local

import domain.model.User

interface UsersDataSource {
    fun addUser(newUser: User)
    fun removeUser(telegramChatId: Long)

    fun getUsers(): List<User>
}