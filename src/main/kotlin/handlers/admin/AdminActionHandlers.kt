package handlers.admin

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
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

            onCommand("add_admin", initialFilter = { it.chat is PrivateChat }) { message ->
                actionsController.addAdmin(message)
            }

            onCommand("show_admins", initialFilter = { it.chat is PrivateChat }) { message ->
                actionsController.showAllAdmins(message)
            }
        }
    }
}