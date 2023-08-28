package ioc

import dagger.Binds
import dagger.Module
import data.local.group.GroupsDataSource
import data.local.group.GroupsRepositoryImpl
import data.local.group.LocalGroupsDataSource
import domain.GroupsRepository

@Module
interface GroupsModule {
    @Binds
    fun groupsDataSource(impl: LocalGroupsDataSource): GroupsDataSource

    @Binds
    fun groupsRepository(impl: GroupsRepositoryImpl): GroupsRepository
}