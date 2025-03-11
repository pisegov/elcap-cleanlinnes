package domain.states

import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import domain.AdminManagedType

sealed interface BotState : State {

    // declare explicitly instead of using null
    data object InitialState: BotState {
        override val context: Any get() = this
    }

    data class CommonMessageSent(
        override val context: IdChatIdentifier,
        val sourceMessage: CommonMessage<MessageContent>,
    ) : BotState

    data class ExpectSharedChatToDelete(
        override val context: IdChatIdentifier,
        val sourceMessage: CommonMessage<TextContent>,
        val chatType: AdminManagedType,
    ) : BotState

    data class CorrectInputSharedChatToDelete(
        override val context: IdChatIdentifier,
        val deletedChatTitle: String,
        val chatType: AdminManagedType,
    ) : BotState

    data class WrongInputSharedChatToDelete(
        override val context: IdChatIdentifier,
        val sourceMessage: CommonMessage<TextContent>,
        val chatType: AdminManagedType,
        val cause: String = "",
    ) : BotState

    data class ExpectTextCall(
        override val context: IdChatIdentifier,
        val sourceMessage: CommonMessage<TextContent>,
    ) : BotState

    data class ExpectedSharedChatToAdd(
        override val context: IdChatIdentifier,
        val sourceMessage: CommonMessage<TextContent>,
        val chatType: AdminManagedType,
    ) : BotState

    data class StopState(override val context: IdChatIdentifier) : BotState

    data class PermissionsDeniedState(override val context: IdChatIdentifier) : BotState
}
