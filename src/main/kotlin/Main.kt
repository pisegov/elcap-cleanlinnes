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

private object EnvVars {
    const val CLEANCAP_BOT_TOKEN = "CLEANCAP_BOT_TOKEN"
    const val CLEANCAP_DATABASE_RELATIVE_PATH = "CLEANCAP_DATABASE_RELATIVE_PATH"
}

suspend fun main() {
    val botToken = requireNotNull(System.getenv(EnvVars.CLEANCAP_BOT_TOKEN)) {
        "There is no bot token in CLEANCAP_BOT_TOKEN env variable"
    }
    val databasePath = requireNotNull(System.getenv(EnvVars.CLEANCAP_DATABASE_RELATIVE_PATH)) {
        "There is no database relative path in CLEANCAP_DATABASE_RELATIVE_PATH env variable"
    }

    val bot = telegramBot(botToken)
    Database.connect("jdbc:sqlite:$databasePath", driver = "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(ReceiversTable)
        SchemaUtils.create(GroupsTable)
        SchemaUtils.create(UsersTable)
        SchemaUtils.create(AdminsTable)
    }

    bot.buildBehaviourWithFSMAndStartLongPolling {
        println(getMe())

        val applicationComponent = DaggerApplicationComponent.factory().create(this)
        val behaviourContext = this
        applicationComponent.apply {
            actionHandlers.forEach { it.setupHandlers(behaviourContext) }
        }
        setMyCommands(
            BotCommand("start", "Show start message"),
            BotCommand("help", "Show help message"),
            BotCommand("call", "Обратиться к сотруднику"),
        )
    }.join()
}