package handlers.user

import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.forwardMessage
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.api.send.withTypingAction
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.parseCommandsWithParams
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.chat.ExtendedPrivateChat
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.extensions.threadIdOrNull
import domain.AdminManagedType
import domain.AdminsRepository
import domain.ReceiversRepository
import domain.UsersRepository
import domain.model.Chat
import domain.model.User
import korlibs.time.DateTime
import korlibs.time.TimezoneOffset
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import states.BotState
import util.ResourceProvider
import javax.inject.Inject

class UserActionsController @Inject constructor(
    private val behaviourContext: BehaviourContext,
    private val receiversRepository: ReceiversRepository,
    private val usersRepository: UsersRepository,
    private val adminsRepository: AdminsRepository,
) {
    suspend fun handleStartCommand(receivedMessage: CommonMessage<TextContent>) {
        saveUser(receivedMessage)
        with(behaviourContext) {
            send(
                receivedMessage.chat, """
                Добро пожаловать в El Cleanliness :)
                Мы рады представить вам чат-бота для обмена информацией о загрязнениях в зале
                
                Сюда вы можете прислать информацию о том, где нужно прибраться, и наши сотрудники моментально об этом узнают
                
                Мы просим вас не присылать лишнего
                В противном случае, мы уже делаем систему банов)
                
                Желаем вам хороших и чистых тренировок :)
            """.trimIndent()
            )
            val isAdmin = async {
                adminsRepository.getAdminsList().map { it.telegramChatId }.contains(receivedMessage.chat.id.chatId)
            }
            val isReceiver = async {
                receiversRepository.getReceiversList().map { it.telegramChatId }
                    .contains(receivedMessage.chat.id.chatId)
            }

            if (isAdmin.await()) {
                send(receivedMessage.chat, ResourceProvider.welcomeMessage(AdminManagedType.Admin))
            }
            if (isReceiver.await()) {
                send(receivedMessage.chat, ResourceProvider.welcomeMessage(AdminManagedType.Receiver))
            }

        }
    }

    suspend fun forwardCallToReceivers(receivedMessage: CommonMessage<MessageContent>) {
        val chatIdentifier = receivedMessage.chat.id

        val receivers = receiversRepository.getReceiversList()
            // If someone send a message and is a receiver, they don't need to receive this message
            .filter {
                it.telegramChatId != chatIdentifier.chatId
            }
        val forwardedSuccessfully = forwardMessageToEveryPerson(receivedMessage, receivers)

        if (forwardedSuccessfully) {
            sendMessageOnSuccessfulForward(chatIdentifier)
        } else {
            reportForwardingError(receivedMessage)
            sendMessageOnUnsuccessfulForward(chatIdentifier)
        }
    }

    suspend fun handleTextCall(state: BotState.ExpectTextCall): BotState? {
        with(behaviourContext) {
            val contentMessage = waitAnyContentMessage().filter { message ->
                message.sameThread(state.sourceMessage)
            }.first()
            val content = contentMessage.content

            return when {
                content is TextContent && content.parseCommandsWithParams().containsKey("cancel") -> {
                    BotState.StopState(state.context)
                }

                else -> {
                    forwardCallToReceivers(contentMessage)

                    // Close states chain and return initial state
                    null
                }
            }
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

    private suspend fun sendMessageOnSuccessfulForward(chatId: IdChatIdentifier) {
        with(behaviourContext) {
            send(
                chatId,
                """
                Ваш запрос успешно передан нашим сотрудникам!
                Спасибо, что обращаете внимание на чистоту в зале! :)
                """.trimIndent()
            )
        }
    }

    private suspend fun sendMessageOnUnsuccessfulForward(chatId: IdChatIdentifier) {
        with(behaviourContext) {
            send(
                chatId,
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

    suspend fun sendHelpMessage(receivedMessage: CommonMessage<MessageContent>) {
        with(behaviourContext) {
            send(
                receivedMessage.chat.id,
                """
                Как пользоваться этим ботом:
                
                Сфотографируйте загрязнение, опишите, куда нужно подойти, и отправьте сообщение :)
                Любая присланная вами фотография с приложенным к ней текстом пересылается сотрудникам
                
                Важно: постарайтесь описывать ваш запрос в одном сообщении, т.к. бот пересылает именно сообщение с фотографией
                Если всё же не получилось или вы хотите обратиться только текстом, воспользуйтесь командой /call и опишите всё в одном следующем сообщении
                
                Команда /help выводит это сообщение
                
            """.trimIndent()
            )
        }
    }
}