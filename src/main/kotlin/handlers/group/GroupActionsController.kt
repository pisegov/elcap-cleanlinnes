package handlers.group

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.ChatEvents.LeftChatMemberEvent
import dev.inmo.tgbotapi.types.message.ChatEvents.NewChatMembers
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage
import domain.GroupsRepository
import domain.model.Group
import kotlinx.coroutines.launch
import javax.inject.Inject

class GroupActionsController @Inject constructor(
    private val behaviourContext: BehaviourContext,
    private val groupsRepository: GroupsRepository,
) {
    fun handleAddingToChat(receivedMessage: ChatEventMessage<NewChatMembers>) {
        println(receivedMessage.chatEvent)
        println(receivedMessage.chat)
        val chat = receivedMessage.chat as GroupChat
        behaviourContext.launch {
            groupsRepository.addGroup(
                Group(
                    telegramChatId = chat.id.chatId,
                    chatTitle = chat.title,
                )
            )
        }
    }

    fun handleRemovingFromChat(receivedMessage: ChatEventMessage<LeftChatMemberEvent>) {
        println(receivedMessage.chatEvent)
        println(receivedMessage.chat)
        behaviourContext.launch {
            groupsRepository.removeGroup(receivedMessage.chat.id.chatId)
        }
    }
}