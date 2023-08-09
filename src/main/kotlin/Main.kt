import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.types.BotCommand
import ioc.DaggerApplicationComponent
import util.BOT_TOKEN

suspend fun main() {
    val bot = telegramBot(BOT_TOKEN)

    bot.buildBehaviourWithLongPolling {
        println(getMe())

        val applicationComponent = DaggerApplicationComponent.factory().create(this)
        applicationComponent.apply {
            userActionHandlers.setupHandlers()
            adminActionHandlers.setupHandlers()
            chatActionHandlers.setupHandlers()
        }
        setMyCommands(
            BotCommand("start", "Show start message"),
            BotCommand("help", "Show help message"),
            BotCommand("add_receiver", "Добавить получателя (пользователя или чат)"),
            BotCommand("remove_receiver", "Удалить получателя"),
            BotCommand("show_receivers", "Показать получателей"),
            BotCommand("call", "Обратиться к сотруднику"),
            BotCommand("add_admin", "Добавить админа"),
            BotCommand("remove_admin", "Удалить админа"),
        )
    }.join()
}