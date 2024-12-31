import data.local.admin.AdminsTable
import data.local.group.GroupsTable
import data.local.receiver.ReceiversTable
import data.local.user.UsersTable
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithFSMAndStartLongPolling
import dev.inmo.tgbotapi.types.BotCommand
import ioc.DaggerApplicationComponent
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


suspend fun main() {
    val bot = telegramBot(BOT_TOKEN)

    bot.buildBehaviourWithFSMAndStartLongPolling {
        println(getMe())

        Database.connect("jdbc:sqlite:data.db", driver = "org.sqlite.JDBC")

        transaction {
            SchemaUtils.create(ReceiversTable)
            SchemaUtils.create(GroupsTable)
            SchemaUtils.create(UsersTable)
            SchemaUtils.create(AdminsTable)
        }

        val applicationComponent = DaggerApplicationComponent.factory().create(this)
        applicationComponent.apply {
            actionHandlers.forEach { it.setupHandlers() }
        }
        setMyCommands(
            BotCommand("start", "Show start message"),
            BotCommand("help", "Show help message"),
            BotCommand("call", "Обратиться к сотруднику"),
        )
    }.join()
}