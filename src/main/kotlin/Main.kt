import data.ChatsRepositoryImpl
import data.ReceiversRepositoryImpl
import data.UsersRepositoryImpl
import data.local.ChatsDataSource
import data.local.UsersDataSource
import data.local.in_memory.InMemoryChatsDataSource
import data.local.in_memory.InMemoryDataSourceImpl
import data.local.in_memory.InMemoryUsersDataSource
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.types.BotCommand
import domain.ChatsRepository
import domain.ReceiversRepository
import domain.UsersRepository
import handlers.ActionHandlers
import handlers.admin.AdminActionHandlers
import handlers.chat.ChatActionHandlers
import handlers.user.UserActionHandlers
import util.BOT_TOKEN

val usersDataSource: UsersDataSource = InMemoryUsersDataSource()
val chatsDataSource: ChatsDataSource = InMemoryChatsDataSource()
val usersRepository: UsersRepository = UsersRepositoryImpl(usersDataSource)
val chatsRepository: ChatsRepository = ChatsRepositoryImpl(chatsDataSource)
val receiversRepository: ReceiversRepository =
    ReceiversRepositoryImpl(InMemoryDataSourceImpl(usersDataSource, chatsDataSource))

suspend fun main() {
    val bot = telegramBot(BOT_TOKEN)

    bot.buildBehaviourWithLongPolling {
        println(getMe())

        val handlers: List<ActionHandlers> = listOf(
            AdminActionHandlers(this),
            ChatActionHandlers(this, chatsRepository),
            UserActionHandlers(this)
        )
        handlers.forEach { holder -> holder.setupHandlers() }

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