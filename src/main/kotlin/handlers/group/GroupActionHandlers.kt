package handlers.group

import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onLeftChatMember
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onNewChatMembers
import domain.states.BotState
import handlers.ActionHandlers
import javax.inject.Inject

class GroupActionHandlers @Inject constructor(
    private val actionsController: GroupActionsController,
) :
    ActionHandlers {
    override suspend fun setupHandlers(behaviourContext: DefaultBehaviourContextWithFSM<BotState>) {
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