package handlers.admin

import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestGroupButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestUserButton
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.chat.ExtendedChat
import dev.inmo.tgbotapi.types.chat.ExtendedPrivateChat
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.PrivateEventMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.request.ChatShared
import dev.inmo.tgbotapi.types.request.ChatSharedRequest
import dev.inmo.tgbotapi.types.request.RequestId
import dev.inmo.tgbotapi.types.request.UserShared
import dev.inmo.tgbotapi.utils.row
import domain.ReceiversRepository
import domain.states.InsertionState
import kotlinx.coroutines.launch
import javax.inject.Inject

class AdminActionsController @Inject constructor(
    private val behaviourContext: BehaviourContext,
    private val receiversRepository: ReceiversRepository,
) {

    private val shareChatKeyboard: ReplyKeyboardMarkup by lazy {
        val requestIdUserAny = RequestId(2)
        val requestIdGroup = RequestId(12)
        replyKeyboard(
            resizeKeyboard = true,
            oneTimeKeyboard = true,
        ) {
            row {
                requestUserButton("Добавить пользователя ", requestIdUserAny)
                requestGroupButton("Добавить группу", requestIdGroup)
            }
        }
    }

    private suspend fun <T> withAdminCheck(block: suspend () -> T): T {
        // Do admin check
        // throw exception if it's not succeeded
        return block.invoke()
    }

    suspend fun showReceivers(message: CommonMessage<TextContent>) = withAdminCheck {
        behaviourContext.reply(
            message,
            receiversRepository.getReceiversList().toString(),
        )
    }

    suspend fun addReceiver(receivedMessage: CommonMessage<TextContent>) = withAdminCheck {
        behaviourContext.reply(
            receivedMessage,
            "Here possible requests buttons:",
            replyMarkup = shareChatKeyboard,
        )

    }

    suspend fun handleSharedChat(receivedMessage: PrivateEventMessage<out ChatSharedRequest>) = withAdminCheck {

        val chatEvent = receivedMessage.chatEvent
        val chatIdentifier = chatEvent.chatId as ChatId
        val replyString = StringBuilder()


        behaviourContext.launch {
            val insertionState = receiversRepository.addReceiver(chatIdentifier.chatId)

            val chatTitle = runCatchingSafely {
                val chat = behaviourContext.getChat(chatIdentifier)
                getChatTitle(chat)
            }.getOrDefault("")

            when (insertionState) {
                is InsertionState.Success -> {
                    replyString.append("Получатель $chatTitle сохранён :)\n")
                    replyString.append(receiverReply(chatEvent, chatTitle.isNotEmpty()))
                }

                is InsertionState.Duplicate -> {
                    replyString.append("Получатель $chatTitle уже добавлен в систему\n")
                    replyString.append(receiverReply(chatEvent, chatTitle.isNotEmpty()))
                }

                is InsertionState.Error -> {
                    replyString.append("Что-то пошло не так, бот не смог добавить получателя в систему :(")
                }
            }

            behaviourContext.reply(
                receivedMessage,
                replyString.replace("\\s{2,}".toRegex(), " "),
                replyMarkup = ReplyKeyboardRemove()
            )
        }
    }

    private fun getChatTitle(chat: ExtendedChat): String {

        return when (chat) {
            is ExtendedPrivateChat -> {
                "${chat.firstName} ${chat.lastName}".trim()
            }

            is GroupChat -> {
                chat.title
            }

            else -> {
                ""
            }
        }
    }

    private fun receiverReply(chatEvent: ChatSharedRequest, chatIsActivated: Boolean): String {
        return when (chatEvent) {
            is UserShared -> {
                when {
                    chatIsActivated -> {
                        "Бот готов пересылать пользователю запросы"
                    }

                    else -> {
                        "Пользователь ещё не активировал чат с ботом\nКак только он это сделает, бот сможет пересылать запросы"
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