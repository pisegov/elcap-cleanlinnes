package handlers

import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import domain.states.BotState

interface ActionHandlers {
    suspend fun setupHandlers(behaviourContext: DefaultBehaviourContextWithFSM<BotState>)
}