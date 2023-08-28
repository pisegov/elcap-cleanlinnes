package states

import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

sealed interface BotState : State {
    data class ExpectSharedAdminToDelete(
        override val context: IdChatIdentifier,
        val sourceMessage: CommonMessage<TextContent>,
    ) : BotState

    data class CorrectInputSharedAdminToDelete(
        override val context: IdChatIdentifier,
        val deletedAdminFullName: String,
    ) : BotState

    data class WrongInputSharedAdminToDelete(
        override val context: IdChatIdentifier,
        val sourceMessage: CommonMessage<TextContent>,
        val cause: String = "",
    ) : BotState

    data class ExpectSharedReceiverToDelete(
        override val context: IdChatIdentifier,
        val sourceMessage: CommonMessage<TextContent>,
    ) : BotState

    data class CorrectInputSharedReceiverToDelete(
        override val context: IdChatIdentifier,
        val deletedReceiverChatTitle: String,
    ) : BotState

    data class WrongInputSharedReceiverToDelete(
        override val context: IdChatIdentifier,
        val sourceMessage: CommonMessage<TextContent>,
        val cause: String = "",
    ) : BotState

    data class StopState(override val context: IdChatIdentifier) : BotState

    data class PermissionsDeniedState(override val context: IdChatIdentifier) : BotState
}
