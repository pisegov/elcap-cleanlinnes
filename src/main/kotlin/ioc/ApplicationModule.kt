package ioc

import dagger.Binds
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import states.BotState
import dagger.Module

@Module
interface ApplicationModule {
    @Binds
    fun behaviourContextWithFSM(impl: DefaultBehaviourContextWithFSM<BotState>): BehaviourContextWithFSM<BotState>

    @Binds
    fun behaviourContext(impl: DefaultBehaviourContextWithFSM<BotState>): BehaviourContext

}