package ioc

import dagger.Binds
import dagger.Module
import data.local.admin.AdminsDataSource
import data.local.admin.AdminsRepositoryImpl
import data.local.admin.LocalAdminsDataSource
import domain.AdminsRepository

@Module
interface AdminsModule {
    @Binds
    fun adminsDataSource(impl: LocalAdminsDataSource): AdminsDataSource

    @Binds
    fun adminsRepository(impl: AdminsRepositoryImpl): AdminsRepository
}