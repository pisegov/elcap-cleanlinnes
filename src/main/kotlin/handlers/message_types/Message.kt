package handlers.message_types

import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.*
import domain.model.Chat
import handlers.user.UserMessageSender

sealed interface Message {
    val telegramMessage: CommonMessage<*>
}

interface ForwardableMessage: Message {
    suspend fun forward(chat: Chat, messageSender: UserMessageSender)
}

class SingleMediaContentMessage(
    override val telegramMessage: CommonMessage<VisualMediaGroupPartContent>,
): ForwardableMessage {
    override suspend fun forward(chat: Chat, messageSender: UserMessageSender) {
        messageSender.forwardSingleMediaContentMessage(message = telegramMessage, chat = chat)
    }
}

class VisualMediaGroupContentMessage(
    override val telegramMessage: CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>,
): ForwardableMessage {
    override suspend fun forward(chat: Chat, messageSender: UserMessageSender) {
        messageSender.resendMediaGroup(message = telegramMessage, chat = chat)
    }
}

class ExplicitCallMessage(
    override val telegramMessage: CommonMessage<MessageContent>,
): ForwardableMessage {
    override suspend fun forward(chat: Chat, messageSender: UserMessageSender) {
        messageSender.forwardSingleMediaContentMessage(message = telegramMessage, chat = chat)
    }
}

class SimpleTextMessage(
    override val telegramMessage: CommonMessage<TextedContent>,
): Message

class UnsupportedMessage(
    override val telegramMessage: CommonMessage<*>,
): Message