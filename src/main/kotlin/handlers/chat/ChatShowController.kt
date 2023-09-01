package handlers.chat

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import domain.AdminManagedRepositoriesProvider
import domain.AdminManagedType
import domain.model.Chat
import domain.states.BotState
import util.ResourceProvider
import javax.inject.Inject

class ChatShowController @Inject constructor(
    private val behaviourContext: BehaviourContextWithFSM<BotState>,
    private val chatsRepositoryProvider: AdminManagedRepositoriesProvider,
) {
    suspend fun showChatsList(message: CommonMessage<TextContent>, chatType: AdminManagedType) {
        val repository = chatsRepositoryProvider.provide(chatType)
        val replyString = StringBuilder()

        val adminsList = repository.getChatsList()
        val activeAdmins = adminsList.filter { it.chatTitle.isNotEmpty() }
        val notActiveAdmins = adminsList.filter { it.chatTitle.isEmpty() }

        if (adminsList.isEmpty()) {
            replyString.append(ResourceProvider.chatsListIsEmpty(chatType))
        } else {
            if (activeAdmins.isNotEmpty()) {
                replyString.append("${ResourceProvider.activeChats(chatType)}:\n\n")
                activeAdmins.forEach { chat: Chat ->
                    replyString.append("${chat.chatTitle}\nTelegram chat id: ${chat.telegramChatId}\n\n")
                }
            }
            if (notActiveAdmins.isNotEmpty()) {
                replyString.append("\n${ResourceProvider.notActiveChats(chatType)}:\n\n")
                notActiveAdmins.forEach { chat: Chat ->
                    replyString.append("Telegram chat id: ${chat.telegramChatId}\n")
                }
            }
        }
        behaviourContext.reply(
            message,
            replyString.toString(),
        )
    }
}