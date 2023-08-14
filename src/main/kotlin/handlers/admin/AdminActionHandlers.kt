package handlers.admin

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onChatShared
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onUserShared
import dev.inmo.tgbotapi.types.chat.PrivateChat
import handlers.ActionHandlers
import javax.inject.Inject

class AdminActionHandlers @Inject constructor(
    private val behaviourContext: BehaviourContext,
    private val actionsController: AdminActionsController,
) : ActionHandlers {
    override suspend fun setupHandlers() {
        with(behaviourContext) {
            onCommand("show_receivers", initialFilter = { it.chat is PrivateChat }) { message ->
                actionsController.showReceivers(message)
            }

            onCommand("add_receiver", initialFilter = { it.chat is PrivateChat }) { message ->
                actionsController.addReceiver(message)
            }

            onUserShared { message ->
                actionsController.handleSharedChat(message)
            }

            onChatShared { message ->
                actionsController.handleSharedChat(message)
            }
        }
    }
}