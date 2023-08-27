package ioc

import dagger.BindsInstance
import dagger.Component
import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import handlers.admin.AdminActionHandlers
import handlers.admin.PermissionsChecker
import handlers.admin.SharedAdminHandler
import handlers.chat.ChatActionHandlers
import handlers.receiver.ReceiverActionHandlers
import handlers.receiver.SharedReceiverHandler
import handlers.user.UserActionHandlers
import states.BotState

@Component(modules = [ApplicationModule::class, AdminsModule::class, ReceiversModule::class, ChatsModule::class, UsersModule::class])
@ApplicationScope
interface ApplicationComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance behaviourContext: DefaultBehaviourContextWithFSM<BotState>,
        ): ApplicationComponent
    }

    val adminActionHandlers: AdminActionHandlers
    val userActionHandlers: UserActionHandlers
    val chatActionHandlers: ChatActionHandlers
    val receiverActionHandlers: ReceiverActionHandlers

    val sharedAdminHandler: SharedAdminHandler
    val sharedReceiverHandler: SharedReceiverHandler

    val permissionsChecker: PermissionsChecker
}