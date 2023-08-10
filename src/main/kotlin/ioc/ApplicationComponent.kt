package ioc

import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import data.ChatsRepositoryImpl
import data.ReceiversRepositoryImpl
import data.UsersRepositoryImpl
import data.local.ChatsDataSource
import data.local.ReceiversDataSource
import data.local.UsersDataSource
import data.local.db.LocalChatsDataSource
import data.local.in_memory.InMemoryReceiversDataSource
import data.local.in_memory.InMemoryUsersDataSource
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import domain.ChatsRepository
import domain.ReceiversRepository
import domain.UsersRepository
import handlers.admin.AdminActionHandlers
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
}


@Scope
annotation class ApplicationScope

@Module
interface ApplicationModule {
    @Binds
    fun chatsDataSource(impl: LocalChatsDataSource): ChatsDataSource

    @Binds
    fun usersDataSource(impl: InMemoryUsersDataSource): UsersDataSource

    @Binds
    fun receiversDataSource(impl: InMemoryReceiversDataSource): ReceiversDataSource

    @Binds
    fun chatsRepository(impl: ChatsRepositoryImpl): ChatsRepository

    @Binds
    fun usersRepository(impl: UsersRepositoryImpl): UsersRepository

    @Binds
    fun receiversRepository(impl: ReceiversRepositoryImpl): ReceiversRepository
}