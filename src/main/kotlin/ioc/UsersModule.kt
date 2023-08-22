package ioc

import dagger.Binds
import dagger.Module
import data.local.user.LocalUsersDataSource
import data.local.user.UsersDataSource
import data.local.user.UsersRepositoryImpl
import domain.UsersRepository

@Module
interface UsersModule {
    @Binds
    fun usersDataSource(impl: LocalUsersDataSource): UsersDataSource

    @Binds
    fun usersRepository(impl: UsersRepositoryImpl): UsersRepository
}