package handlers.chat

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onLeftChatMember
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onNewChatMembers
import handlers.ActionHandlers
import javax.inject.Inject

class ChatActionHandlers @Inject constructor(
    private val behaviourContext: BehaviourContext,
    private val actionsController: ChatActionsController,
) :
    ActionHandlers {
    override suspend fun setupHandlers() {
        with(behaviourContext) {
            onNewChatMembers { message ->
                actionsController.handleAddingToChat(message)
            }

            onLeftChatMember { message ->
                actionsController.handleRemovingFromChat(message)
            }
        }
    }
}