package ioc

import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import data.local.admin.AdminsDataSource
import data.local.admin.AdminsRepositoryImpl
import data.local.admin.LocalAdminsDataSource
import data.local.chat.ChatsDataSource
import data.local.chat.ChatsRepositoryImpl
import data.local.chat.LocalChatsDataSource
import data.local.receiver.LocalReceiversDataSource
import data.local.receiver.ReceiversDataSource
import data.local.receiver.ReceiversRepositoryImpl
import data.local.user.LocalUsersDataSource
import data.local.user.UsersDataSource
import data.local.user.UsersRepositoryImpl
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import domain.AdminsRepository
import domain.ChatsRepository
import domain.ReceiversRepository
import domain.UsersRepository
import handlers.admin.AdminActionHandlers
import handlers.admin.SharedAdminHandler
import handlers.admin.SharedReceiverHandler
import handlers.chat.ChatActionHandlers
import handlers.user.UserActionHandlers
import javax.inject.Scope

@Component(modules = [ApplicationModule::class])
@ApplicationScope
interface ApplicationComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance behaviourContext: BehaviourContext,
        ): ApplicationComponent
    }

    val adminActionHandlers: AdminActionHandlers
    val userActionHandlers: UserActionHandlers
    val chatActionHandlers: ChatActionHandlers

    val sharedAdminHandler: SharedAdminHandler
    val sharedReceiverHandler: SharedReceiverHandler
}


@Scope
annotation class ApplicationScope

@Module
interface ApplicationModule {
    @Binds
    fun chatsDataSource(impl: LocalChatsDataSource): ChatsDataSource

    @Binds
    fun usersDataSource(impl: LocalUsersDataSource): UsersDataSource

    @Binds
    fun receiversDataSource(impl: LocalReceiversDataSource): ReceiversDataSource

    @Binds
    fun adminsDataSource(impl: LocalAdminsDataSource): AdminsDataSource

    @Binds
    fun chatsRepository(impl: ChatsRepositoryImpl): ChatsRepository

    @Binds
    fun usersRepository(impl: UsersRepositoryImpl): UsersRepository

    @Binds
    fun receiversRepository(impl: ReceiversRepositoryImpl): ReceiversRepository

    @Binds
    fun adminsRepository(impl: AdminsRepositoryImpl): AdminsRepository

}