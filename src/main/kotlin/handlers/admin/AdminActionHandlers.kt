package handlers.admin

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onChatShared
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onUserShared
import dev.inmo.tgbotapi.types.chat.PrivateChat
import handlers.ActionHandlers
import receiversRepository

class AdminActionHandlers(private val behaviourContext: BehaviourContext) : ActionHandlers {

    private val adminActionsController = AdminActionsController(behaviourContext, receiversRepository)

    override suspend fun setupHandlers() {
        behaviourContext.apply {
            onCommand("show_receivers", initialFilter = { it.chat is PrivateChat }) { message ->
                adminActionsController.showReceivers(message)
            }

            onCommand("add_receiver", initialFilter = { it.chat is PrivateChat }) { message ->
                adminActionsController.addReceiver(message)
            }

            onUserShared { message ->
                adminActionsController.handleSharedUser(message)
            }

            onChatShared { message ->
                adminActionsController.handleSharedChat(message)
            }
        }
    }
}