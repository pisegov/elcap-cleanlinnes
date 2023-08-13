package data

import data.local.UsersDataSource
import domain.UsersRepository
import domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(private val dataSource: UsersDataSource) : UsersRepository {
    override suspend fun addUser(newUser: User) = withContext(Dispatchers.IO) {
        dataSource.addUser(newUser)
    }

    override suspend fun removeUser(telegramChatId: Long) = withContext(Dispatchers.IO) {
        dataSource.removeUser(telegramChatId)
    }
}