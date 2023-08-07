package handlers.chat

import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.ChatEvents.LeftChatMemberEvent
import dev.inmo.tgbotapi.types.message.ChatEvents.NewChatMembers
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage
import domain.ChatsRepository
import domain.model.Chat

class ChatActionsController(private val chatsRepository: ChatsRepository) {
    fun handleAddingToChat(receivedMessage: ChatEventMessage<NewChatMembers>) {
        println(receivedMessage.chatEvent)
        println(receivedMessage.chat)
        val chat = receivedMessage.chat as GroupChat
        chatsRepository.addChat(
            Chat(
                telegramChatId = chat.id.chatId,
                title = chat.title,
            )
        )
    }

    fun handleRemovingFromChat(receivedMessage: ChatEventMessage<LeftChatMemberEvent>) {
        println(receivedMessage.chatEvent)
        println(receivedMessage.chat)
        chatsRepository.removeChat(receivedMessage.chat.id.chatId)
    }
}