package handlers.user

import dev.inmo.tgbotapi.extensions.utils.textContentOrNull
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.MediaGroupContent
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.VisualMediaGroupPartContent
import dev.inmo.tgbotapi.utils.bold
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.italic
import domain.AdminManagedType
import domain.model.Chat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import util.ResourceProvider
import util.getContentWithUserMention
import util.toDomainModel
import javax.inject.Inject

class UserMessageSender @Inject constructor(
    private val telegramMessageSender: TelegramMessageSender,
) {
    suspend fun sendWelcomeMessage(chatId: Long): Result<ContentMessage<TextContent>> {
        val message = """
                Добро пожаловать в бот чистоты El Capitan 👋

                Отправляйте информацию о любом загрязнении и наши сотрудники моментально об этом узнают 🤝

                Просим не присылать лишнего, иначе мы вас заблокируем 🫢

                Желаем вам хороших и чистых тренировок ❤️
            """.trimIndent()
        return telegramMessageSender.sendMessage(chatId, message)
    }

    suspend fun sendAdminWelcomeMessage(chatId: Long): Result<ContentMessage<TextContent>> {
        return telegramMessageSender.sendMessage(
            chatId = chatId,
            messageText = ResourceProvider.welcomeMessage(AdminManagedType.Admin),
        )
    }

    suspend fun sendReceiverWelcomeMessage(chatId: Long): Result<ContentMessage<TextContent>> {
        return telegramMessageSender.sendMessage(
            chatId = chatId,
            messageText = ResourceProvider.welcomeMessage(AdminManagedType.Receiver),
        )
    }

    suspend fun sendHelpMessage(chatId: Long): Result<ContentMessage<TextContent>> {
        val textSources = buildEntities("\n\n") {
            bold("Инструкция:")

            +"""
            - сфотографируйте загрязнение, 
            - опишите куда нужно подойти, 
            - отправьте сообщение. 
            """.trimIndent()

            italic(
                buildEntities(" ") {
                    +"""
                    Важно: постарайтесь описывать ваш запрос в одном сообщении, т.к. бот пересылает только сообщение с фотографией.
                    Если всё же не получилось или вы хотите обратиться только текстом, воспользуйтесь командой
                    """.trimIndent()
                    bold("/call")
                    +"и опишите всё в одном следующем сообщении."
                }
            )
        }
        return telegramMessageSender.sendMessage(chatId = chatId, textSources = textSources)
    }

    suspend fun sendMessageOnSuccessfulForward(chatId: Long): Result<ContentMessage<TextContent>> {
        return telegramMessageSender.sendMessage(
            chatId = chatId, messageText = """
                                Ваш запрос успешно передан нашим сотрудникам!
                                Спасибо, что обращаете внимание на чистоту в зале! :)
                                """.trimIndent()
        )
    }

    suspend fun sendMessageOnUnsuccessfulForward(chatId: Long): Result<ContentMessage<TextContent>> {
        return telegramMessageSender.sendMessage(
            chatId = chatId, messageText = """
                                К сожалению, ваш запрос не был передан нашим сотрудникам :(
                                Администраторы уведомлены об ошибке и вскоре мы исправим её
                                Вы можете обратиться к нашим сотрудникам в зале или на ресепшен, чтобы передать информацию
                                
                                Спасибо, что обращаете внимание на чистоту в зале! :)
                                """.trimIndent()
        )
    }

    suspend fun sendMessage(chatId: Long, messageText: String): Result<ContentMessage<TextContent>> {
        return telegramMessageSender.sendMessage(chatId = chatId, messageText = messageText)
    }

    fun forwardSingleMediaContentMessage(
        message: CommonMessage<MessageContent>,
        chat: Chat,
    ): Flow<Result<Any>> {
        return flow {
            telegramMessageSender.forwardMessage(
                chatId = chat.telegramChatId,
                message = message,
            ).onSuccess {
                emit(Result.success(it))
                return@flow
            }.onFailure {
                emit(Result.failure(it))
            }

            telegramMessageSender.sendMessage(
                chatId = chat.telegramChatId,
                messageText = message.content.textContentOrNull()?.text.orEmpty(),
            ).onSuccess {
                emit(Result.success(it))
                return@flow
            }.onFailure {
                emit(Result.failure(it))
            }
        }
    }

    fun resendMediaGroup(
        message: CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>,
        chat: Chat,
    ): Flow<Result<ContentMessage<MediaGroupContent<VisualMediaGroupPartContent>>>> {
        val fromUser = (message.chat as PrivateChat).toDomainModel()
        val newContent = message.getContentWithUserMention(fromUser)

        return flow {
            emit(
                telegramMessageSender.sendVisualMediaGroup(
                    chatId = chat.telegramChatId,
                    media = newContent,
                )
            )
        }
    }
}
