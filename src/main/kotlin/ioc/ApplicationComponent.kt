package ioc

import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import data.AdminsRepositoryImpl
import data.ChatsRepositoryImpl
import data.ReceiversRepositoryImpl
import data.UsersRepositoryImpl
import data.local.*
import data.local.db.LocalAdminsDataSource
import data.local.db.LocalChatsDataSource
import data.local.db.LocalReceiversDataSource
import data.local.db.LocalUsersDataSource
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