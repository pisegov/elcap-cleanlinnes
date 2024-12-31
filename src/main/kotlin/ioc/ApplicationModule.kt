package ioc

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import domain.states.BotState
import handlers.ActionHandlers
import handlers.admin.AdminActionHandlers
import handlers.group.GroupActionHandlers
import handlers.user.UserActionHandlers

@Module
interface ApplicationModule {
    @Binds
    fun behaviourContextWithFSM(impl: DefaultBehaviourContextWithFSM<BotState>): BehaviourContextWithFSM<BotState>

    @Binds
    fun behaviourContext(impl: DefaultBehaviourContextWithFSM<BotState>): BehaviourContext

    @Binds
    @IntoSet
    fun bindUserActionHandlers(impl: UserActionHandlers): ActionHandlers

    @Binds
    @IntoSet
    fun bindGroupActionHandlers(impl: GroupActionHandlers): ActionHandlers

    @Binds
    @IntoSet
    fun bindAdminActionHandlers(impl: AdminActionHandlers): ActionHandlers
}