package handlers.user

import dev.inmo.tgbotapi.extensions.utils.textContentOrNull
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.MediaGroupContent
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.VisualMediaGroupPartContent
import domain.AdminManagedType
import domain.model.Chat
import util.*
import javax.inject.Inject

class UserMessageSender @Inject constructor(
    private val telegramMessageSender: TelegramMessageSender,
) {
    suspend fun sendWelcomeMessage(chatId: Long): Result<ContentMessage<TextContent>> {
        val message = """
                Добро пожаловать в El Cleanliness :)
                Мы рады представить вам чат-бота для обмена информацией о загрязнениях в зале
                
                Сюда вы можете прислать информацию о том, где нужно прибраться, и наши сотрудники моментально об этом узнают
                
                Мы просим вас не присылать лишнего
                В противном случае, мы уже делаем систему банов)
                
                Желаем вам хороших и чистых тренировок :)
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
        val message = """
                Как пользоваться этим ботом:
                
                Сфотографируйте загрязнение, опишите, куда нужно подойти, и отправьте сообщение :)
                Любая присланная вами фотография с приложенным к ней текстом пересылается сотрудникам
                
                Важно: постарайтесь описывать ваш запрос в одном сообщении, т.к. бот пересылает именно сообщение с фотографией
                Если всё же не получилось или вы хотите обратиться только текстом, воспользуйтесь командой /call и опишите всё в одном следующем сообщении
                
                Команда /help выводит это сообщение
                
            """.trimIndent()
        return telegramMessageSender.sendMessage(chatId = chatId, messageText = message)
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

    suspend fun forwardSingleMediaContentMessage(
        message: CommonMessage<MessageContent>,
        chat: Chat,
    ): Result<Any> {
        telegramMessageSender.forwardMessage(
            chatId = chat.telegramChatId,
            message = message,
        ).onSuccess {
            return Result.success(it)
        }.onFailure {
            println(it.message)
            return telegramMessageSender.createResend(
                chatId = chat.telegramChatId,
                message= message,
            )
        }

        return Result.failure(IllegalStateException("Forwarding went wrong"))
    }

    suspend fun resendMediaGroup(
        message: CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>,
        chat: Chat,
    ): Result<ContentMessage<MediaGroupContent<VisualMediaGroupPartContent>>> {
        val fromUser = (message.chat as PrivateChat).toDomainModel()
        val newContent = message.getContentWithUserMention(fromUser)

        return telegramMessageSender.sendVisualMediaGroup(
            chatId = chat.telegramChatId,
            media = newContent,
        )
    }
}