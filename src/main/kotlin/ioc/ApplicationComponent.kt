package ioc

import dagger.BindsInstance
import dagger.Component
import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import handlers.admin.AdminActionHandlers
import handlers.admin.PermissionsChecker
import handlers.chat.ChatActionHandlers
import handlers.receiver.SharedReceiverHandler
import handlers.user.UserActionHandlers
import states.BotState

@Component(modules = [ApplicationModule::class, AdminsModule::class, ReceiversModule::class, GroupsModule::class, UsersModule::class])
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
    val groupActionHandlers: ChatActionHandlers

    val sharedReceiverHandler: SharedReceiverHandler

    val permissionsChecker: PermissionsChecker
}