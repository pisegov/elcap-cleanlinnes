package handlers.chat

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitChatSharedRequestEventsMessages
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.request.ChatShared
import dev.inmo.tgbotapi.types.request.ChatSharedRequest
import dev.inmo.tgbotapi.types.request.UserShared
import domain.AdminManagedRepositoriesProvider
import domain.AdminManagedType
import domain.states.BotState
import domain.states.BotState.ExpectedSharedChatToAdd
import domain.states.InsertionState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import util.KeyboardBuilder
import util.ResourceProvider
import util.getChatTitle
import util.removedDoubleSpaces
import javax.inject.Inject

class ChatAddController @Inject constructor(
    private val behaviourContext: BehaviourContext,
    private val chatsRepositoryProvider: AdminManagedRepositoriesProvider,
) {
    suspend fun addChat(receivedMessage: CommonMessage<TextContent>, chatType: AdminManagedType) {
        with(behaviourContext) {
            reply(
                receivedMessage,
                "${ResourceProvider.useButtonToShareChat(chatType)}:",
                replyMarkup = KeyboardBuilder.shareChatKeyboard(chatType),
            )

            val shared = waitChatSharedRequestEventsMessages().filter { message ->
                message.sameThread(receivedMessage)
            }.first()

            handleSharedChat(shared, chatType)
        }
    }

    private fun handleSharedChat(eventMessage: ChatEventMessage<out ChatSharedRequest>, chatType: AdminManagedType) {
        val chatsRepository = chatsRepositoryProvider.provide(chatType)
        val chatEvent = eventMessage.chatEvent
        val chatIdentifier: ChatId = chatEvent.chatId as ChatId
        val chatId: Long = chatIdentifier.chatId

        with(behaviourContext) {
            launch {
                val insertionState: InsertionState = chatsRepository.addChat(chatId)
                val chatTitle: String = chatIdentifier.getChatTitle(behaviourContext)
                val replyString: String = getReplyString(insertionState, chatEvent, chatTitle, chatType)

                launch {
                    if (insertionState is InsertionState.Success) {
                        send(chatIdentifier, ResourceProvider.welcomeMessage(chatType))
                    }
                }
                reply(
                    eventMessage,
                    replyString,
                    replyMarkup = ReplyKeyboardRemove()
                )
            }
        }
    }

    private fun getReplyString(
        insertionState: InsertionState,
        chatEvent: ChatSharedRequest,
        chatTitle: String,
        chatType: AdminManagedType,
    ): String {
        val replyString = StringBuilder()
        when (insertionState) {
            is InsertionState.Success -> {
                replyString.append("${ResourceProvider.chatTypeTitle(chatType)} $chatTitle сохранён :)\n")
                replyString.append(chatReply(chatIsActivated = chatTitle.isNotBlank(), chatEvent, chatType))
            }

            is InsertionState.Duplicate -> {
                replyString.append("${ResourceProvider.chatTypeTitle(chatType)}  $chatTitle уже добавлен в систему :)\n")
                replyString.append(chatReply(chatIsActivated = chatTitle.isNotBlank(), chatEvent, chatType))
            }

            is InsertionState.Error -> {
                replyString.append("Что-то пошло не так, бот не смог добавить ${ResourceProvider.whom(chatType)} в систему :(")
            }
        }

        return replyString.removedDoubleSpaces()
    }

    private fun chatReply(chatIsActivated: Boolean, chatEvent: ChatSharedRequest, chatType: AdminManagedType): String {
        return when (chatEvent) {
            is UserShared -> {
                when {
                    chatIsActivated -> {
                        ResourceProvider.userIsReady(chatType)
                    }

                    else -> {
                        ResourceProvider.userIsNotReady(chatType)
                    }
                }
            }

            is ChatShared -> {
                when {
                    chatIsActivated -> {
                        "Бот готов пересылать запросы в этот чат"
                    }

                    else -> {
                        "Бот ещё не добавлен в этот чат\nБот сможет пересылать запросы, как только будет добавлен в чат"
                    }
                }
            }

            else -> {
                ""
            }
        }
    }
}