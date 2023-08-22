package ioc

import dagger.BindsInstance
import dagger.Component
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import handlers.admin.AdminActionHandlers
import handlers.admin.SharedAdminHandler
import handlers.admin.SharedReceiverHandler
import handlers.chat.ChatActionHandlers
import handlers.user.UserActionHandlers

@Component(modules = [AdminsModule::class, ReceiversModule::class, ChatsModule::class, UsersModule::class])
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