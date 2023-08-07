package handlers.chat

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onLeftChatMember
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onNewChatMembers
import domain.ChatsRepository
import handlers.ActionHandlers

class ChatActionHandlers(private val behaviourContext: BehaviourContext, chatsRepository: ChatsRepository) :
    ActionHandlers {
    private val chatActionsController = ChatActionsController(chatsRepository)
    override suspend fun setupHandlers() {
        behaviourContext.apply {
            onNewChatMembers { message ->
                chatActionsController.handleAddingToChat(message)
            }

            onLeftChatMember { message ->
                chatActionsController.handleRemovingFromChat(message)
            }
        }
    }
}