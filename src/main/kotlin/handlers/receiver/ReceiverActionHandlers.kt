package handlers.receiver

import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.chat.PrivateChat
import handlers.ActionHandlers
import states.BotState
import javax.inject.Inject

class ReceiverActionHandlers @Inject constructor(
    private val behaviourContext: DefaultBehaviourContextWithFSM<BotState>,
    private val actionsController: ReceiverActionsController,
) : ActionHandlers {
    override suspend fun setupHandlers() {
        with(behaviourContext) {
            onCommand("show_receivers", initialFilter = { it.chat is PrivateChat }) { message ->
                actionsController.showReceivers(message)
            }

            onCommand("add_receiver", initialFilter = { it.chat is PrivateChat }) { message ->
                actionsController.addReceiver(message)
            }
        }
    }
}