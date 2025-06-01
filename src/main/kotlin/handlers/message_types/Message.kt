package handlers.message_types

import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.*
import domain.model.Chat
import handlers.user.UserMessageSender
import kotlinx.coroutines.flow.Flow

sealed interface Message {
    val telegramMessage: CommonMessage<*>

    suspend fun forward(chat: Chat, messageSender: UserMessageSender): Flow<Result<Any>> {
        return messageSender.forwardSingleMediaContentMessage(message = telegramMessage, chat = chat)
    }
}

sealed interface SupportedMessage: Message

sealed interface UnsupportedMessage: Message {
    suspend fun getAnswerMessageText(): String
}

class SingleMediaContentMessage(
    override val telegramMessage: CommonMessage<MessageContent>,
): SupportedMessage

class VisualMediaGroupContentMessage(
    override val telegramMessage: CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>,
): SupportedMessage {
    override suspend fun forward(
        chat: Chat,
        messageSender: UserMessageSender,
    ): Flow<Result<ContentMessage<MediaGroupContent<VisualMediaGroupPartContent>>>> {
        return messageSender.resendMediaGroup(message = telegramMessage, chat = chat)
    }
}

class SimpleTextMessage(
    override val telegramMessage: CommonMessage<TextedContent>,
): UnsupportedMessage {
    override suspend fun getAnswerMessageText(): String {
        return """
            Бот пересылает сообщения только с фото или видео
            
            Если вы хотите обратиться только текстом, воспользуйтесь командой /call и опишите всё в одном следующем сообщении
            
            Используйте команду /help, чтобы узнать, как пользоваться ботом
        """.trimIndent()
    }
}

class DefaultUnsupportedMessage(
    override val telegramMessage: CommonMessage<*>,
): UnsupportedMessage {
    override suspend fun getAnswerMessageText(): String {
        return """
            Бот пересылает сообщения только с фото или видео
            
            Используйте команду /help, чтобы узнать, как пользоваться ботом
        """.trimIndent()
    }
}

class ErrorMessage(
    override val telegramMessage: CommonMessage<*>,
): UnsupportedMessage {
    override suspend fun getAnswerMessageText(): String {
        return """
            Что-то пошло не так :(
            Обратитесь к администратору на ресепшн
        """.trimIndent()
    }
}