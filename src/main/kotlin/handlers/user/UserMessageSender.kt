package handlers.user

import dev.inmo.tgbotapi.extensions.api.forwardMessage
import dev.inmo.tgbotapi.extensions.api.send.media.sendVisualMediaGroup
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MediaGroupContent
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.VisualMediaGroupPartContent
import dev.inmo.tgbotapi.utils.extensions.threadIdOrNull
import domain.AdminManagedType
import domain.model.Chat
import util.ChatId
import util.ResourceProvider
import util.getContentWithUserMention
import util.toDomainModel
import javax.inject.Inject

class UserMessageSender @Inject constructor(
    private val behaviourContext: BehaviourContext,
) {
    suspend fun sendWelcomeMessage(chatId: Long) {
        val message = """
                Добро пожаловать в El Cleanliness :)
                Мы рады представить вам чат-бота для обмена информацией о загрязнениях в зале
                
                Сюда вы можете прислать информацию о том, где нужно прибраться, и наши сотрудники моментально об этом узнают
                
                Мы просим вас не присылать лишнего
                В противном случае, мы уже делаем систему банов)
                
                Желаем вам хороших и чистых тренировок :)
            """.trimIndent()
        behaviourContext.send(ChatId(chatId), message)
    }

    suspend fun sendAdminWelcomeMessage(chatId: Long) {
        behaviourContext.send(ChatId(chatId), ResourceProvider.welcomeMessage(AdminManagedType.Admin))
    }

    suspend fun sendReceiverWelcomeMessage(chatId: Long) {
        behaviourContext.send(ChatId(chatId), ResourceProvider.welcomeMessage(AdminManagedType.Receiver))
    }

    suspend fun sendHelpMessage(chatId: Long) {
        val message = """
                Как пользоваться этим ботом:
                
                Сфотографируйте загрязнение, опишите, куда нужно подойти, и отправьте сообщение :)
                Любая присланная вами фотография с приложенным к ней текстом пересылается сотрудникам
                
                Важно: постарайтесь описывать ваш запрос в одном сообщении, т.к. бот пересылает именно сообщение с фотографией
                Если всё же не получилось или вы хотите обратиться только текстом, воспользуйтесь командой /call и опишите всё в одном следующем сообщении
                
                Команда /help выводит это сообщение
                
            """.trimIndent()
        behaviourContext.send(chatId = ChatId(chatId), text = message)
    }

    suspend fun sendMessageOnSuccessfulForward(chatId: Long) {
        with(behaviourContext) {
            send(
                ChatId(chatId),
                """
                Ваш запрос успешно передан нашим сотрудникам!
                Спасибо, что обращаете внимание на чистоту в зале! :)
                """.trimIndent()
            )
        }
    }

    suspend fun sendMessageOnUnsuccessfulForward(chatId: Long) {
        with(behaviourContext) {
            send(
                ChatId(chatId),
                """
                К сожалению, ваш запрос не был передан нашим сотрудникам :(
                Администраторы уведомлены об ошибке и вскоре мы исправим её
                Вы можете обратиться к нашим сотрудникам в зале или на ресепшен, чтобы передать информацию
                
                Спасибо, что обращаете внимание на чистоту в зале! :)
                """.trimIndent()
            )
        }
    }

    suspend fun sendMessage(chatId: Long, messageText: String) {
        behaviourContext.send(chatId = ChatId(chatId), text = messageText)
    }

    suspend fun forwardSingleMediaContentMessage(
        message: CommonMessage<MessageContent>,
        chat: Chat,
    ) {
        with(behaviourContext) {
            if (message.forwardable) {
                forwardMessage(
                    toChatId = ChatId(chat.telegramChatId),
                    message = message,
                    threadId = message.threadIdOrNull
                )
            } else {
                message.content.createResend(
                    chatId = ChatId(chat.telegramChatId),
                    messageThreadId = message.threadIdOrNull,
                )
            }
        }
    }

    suspend fun resendMediaGroup(
        message: CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>,
        chat: Chat,
    ) {
        val fromUser = (message.chat as PrivateChat).toDomainModel()
        val newContent = message.getContentWithUserMention(fromUser)

        behaviourContext.sendVisualMediaGroup(chatId = ChatId(chat.telegramChatId), media = newContent)
    }
}