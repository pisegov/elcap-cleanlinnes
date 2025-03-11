package handlers.user

import dev.inmo.tgbotapi.extensions.utils.extensions.parseCommandsWithArgs
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent

fun filterOutCommands(messageContent: MessageContent): Boolean {
    val commandList = listOf(
        "start", "help", "call", "cancel",
        "admin", "add_admin", "show_admins", "remove_admin",
        "add_receiver", "show_receivers", "remove_receiver",
    )

    commandList.forEach {
        if (messageContent is TextContent && messageContent.parseCommandsWithArgs().containsKey(it)) {
            return false
        }
    }

    return true
}
