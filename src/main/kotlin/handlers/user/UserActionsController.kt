package handlers.user

import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.forwardMessage
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.api.send.withTypingAction
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.chat.ExtendedPrivateChat
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.extensions.threadIdOrNull
import domain.AdminsRepository
import domain.ReceiversRepository
import domain.UsersRepository
import domain.model.Chat
import domain.model.User
import korlibs.time.DateTime
import korlibs.time.TimezoneOffset
import javax.inject.Inject

class UserActionsController @Inject constructor(
    private val behaviourContext: BehaviourContext,
    private val receiversRepository: ReceiversRepository,
    private val usersRepository: UsersRepository,
    private val adminsRepository: AdminsRepository,
) {
    suspend fun handleStartCommand(receivedMessage: CommonMessage<TextContent>) {
        saveUser(receivedMessage)
        behaviourContext.send(receivedMessage.chat, "Start message")
    }

    suspend fun forwardCallToReceivers(receivedMessage: CommonMessage<MessageContent>) {
        println(receivedMessage.messageId)

        val receivers = receiversRepository.getReceiversList().filter {
            // If someone send a message and is a receiver, they don't need to receive this message
            it.telegramChatId != receivedMessage.chat.id.chatId
        }
        val forwardedSuccessfully = forwardMessageToEveryPerson(receivedMessage, receivers)

        if (forwardedSuccessfully) {
            sendMessageOnSuccessfulForward(receivedMessage)
        } else {
            reportForwardingError(receivedMessage)
            sendMessageOnUnsuccessfulForward(receivedMessage)
        }
    }

    private suspend fun forwardMessageToEveryPerson(
        receivedMessage: CommonMessage<MessageContent>,
        list: List<Chat>,
    ): Boolean {
        var forwardedSuccessfully = false
        list.forEach { chat ->
            behaviourContext.withTypingAction(receivedMessage.chat) {
                if (receivedMessage.forwardable) {
                    runCatchingSafely {
                        forwardMessage(
                            ChatId(chat.telegramChatId),
                            receivedMessage,
                            threadId = receivedMessage.threadIdOrNull
                        )
                    }.onSuccess {
                        forwardedSuccessfully = true
                    }.onFailure {
                        deleteUser(chat.telegramChatId)
                        it.printStackTrace()
                    }
                } else {
                    runCatchingSafely {
                        receivedMessage.content.createResend(
                            ChatId(chat.telegramChatId),
                            messageThreadId = receivedMessage.threadIdOrNull,
                            allowSendingWithoutReply = false
                        )
                    }.onSuccess {
                        forwardedSuccessfully = true
                    }.onFailure {
                        deleteUser(chat.telegramChatId)
                        it.printStackTrace()
                    }
                }
            }
        }
        return forwardedSuccessfully
    }

    private suspend fun sendMessageOnSuccessfulForward(receivedMessage: CommonMessage<MessageContent>) {
        with(behaviourContext) {
            send(
                receivedMessage.chat,
                """
                Ваш запрос успешно передан нашим сотрудникам!
                Спасибо, что обращаете внимание на чистоту в зале! :)
                """.trimIndent()
            )
        }
    }

    private suspend fun sendMessageOnUnsuccessfulForward(receivedMessage: CommonMessage<MessageContent>) {
        with(behaviourContext) {
            send(
                receivedMessage.chat,
                """
                К сожалению, ваш запрос не был передан нашим сотрудникам :(
                Администраторы уведомлены об ошибке и вскоре мы исправим её
                Вы можете обратиться к нашим сотрудникам в зале или на ресепшен, чтобы передать информацию
                
                Спасибо, что обращаете внимание на чистоту в зале! :)
                """.trimIndent()
            )
        }
    }

    private suspend fun reportForwardingError(receivedMessage: CommonMessage<MessageContent>) {
        val adminsList = adminsRepository.getAdminsList()
        val user: PrivateChat = receivedMessage.chat as PrivateChat
        val date: DateTime = receivedMessage.date
        val offset = TimezoneOffset.local(date)
        val dateTimeTz = date.toOffset(offset)

        adminsList.forEach { admin ->
            behaviourContext.withTypingAction(receivedMessage.chat) {
                send(
                    ChatId(admin.telegramChatId),
                    """
                   Не было передано сообщение от посетителя!
                   Посетитель: ${user.firstName} ${user.lastName}
                   Время сообщения: ${dateTimeTz.format("yyyy-MM-dd HH:mm:ss")}
                """.trimIndent()
                )
            }
        }
        forwardMessageToEveryPerson(receivedMessage, adminsList)
    }

    private suspend fun deleteUser(telegramChatId: Long) {
        usersRepository.removeUser(telegramChatId)
        receiversRepository.removeReceiver(telegramChatId)
    }

    private suspend fun saveUser(receivedMessage: CommonMessage<TextContent>) {
        val telegramUser = behaviourContext.getChat(receivedMessage.chat) as ExtendedPrivateChat
        usersRepository.addUser(
            User(
                telegramUser.id.chatId,
                "${telegramUser.firstName} ${telegramUser.lastName}".trim(),
                telegramUser.username?.username
            )
        )
    }
}