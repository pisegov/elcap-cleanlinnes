package ioc

import dagger.Binds
import dagger.Module
import data.local.receiver.LocalReceiversDataSource
import data.local.receiver.ReceiversDataSource
import data.local.receiver.ReceiversRepositoryImpl
import domain.ReceiversRepository

@Module
interface ReceiversModule {
    @Binds
    fun receiversDataSource(impl: LocalReceiversDataSource): ReceiversDataSource

    @Binds
    fun receiversRepository(impl: ReceiversRepositoryImpl): ReceiversRepository
}