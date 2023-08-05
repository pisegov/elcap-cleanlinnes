package data

import data.local.UsersDataSource
import domain.UsersRepository
import domain.model.User

class UsersRepositoryImpl(private val dataSource: UsersDataSource) : UsersRepository {
    override fun addUser(newUser: User) {
        dataSource.addUser(newUser)
    }

    override fun removeUser(telegramChatId: Long) {
        dataSource.removeUser(telegramChatId)
    }
}