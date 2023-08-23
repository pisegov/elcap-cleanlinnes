import constants.BOT_TOKEN
import constants.mysql_password
import constants.mysql_user
import data.local.admin.AdminsTable
import data.local.chat.ChatsTable
import data.local.receiver.ReceiversTable
import data.local.user.UsersTable
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.types.BotCommand
import ioc.DaggerApplicationComponent
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun main() {
    val bot = telegramBot(BOT_TOKEN)

    bot.buildBehaviourWithLongPolling {
        println(getMe())

        Database.connect(
            url = "jdbc:mysql://localhost:3306/cleanliness_bot?useSSL=false&allowPublicKeyRetrieval=true",
            driver = "com.mysql.cj.jdbc.Driver",
            user = mysql_user,
            password = mysql_password
        )

        transaction {
            SchemaUtils.create(ReceiversTable)
            SchemaUtils.create(ChatsTable)
            SchemaUtils.create(UsersTable)
            SchemaUtils.create(AdminsTable)
        }

        val applicationComponent = DaggerApplicationComponent.factory().create(this)
        applicationComponent.apply {
            userActionHandlers.setupHandlers()
            adminActionHandlers.setupHandlers()
            chatActionHandlers.setupHandlers()
        }
        setMyCommands(
            BotCommand("start", "Show start message"),
            BotCommand("help", "Show help message"),
            BotCommand("call", "Обратиться к сотруднику"),

            BotCommand("add_receiver", "Добавить получателя (пользователя или чат)"),
            BotCommand("remove_receiver", "Удалить получателя"),
            BotCommand("show_receivers", "Показать получателей"),

            BotCommand("add_admin", "Добавить админа"),
            BotCommand("show_admins", "Показать админов"),
            BotCommand("remove_admin", "Удалить админа"),
        )
    }.join()
}