import data.local.admin.AdminsTable
import data.local.group.GroupsTable
import data.local.receiver.ReceiversTable
import data.local.user.UsersTable
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithFSM
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startListenWebhooks
import dev.inmo.tgbotapi.types.BotCommand
import io.ktor.server.netty.*
import ioc.DaggerApplicationComponent
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

private object EnvVars {
    const val CLEANCAP_BOT_TOKEN = "CLEANCAP_BOT_TOKEN"
    const val CLEANCAP_DATABASE_RELATIVE_PATH = "CLEANCAP_DATABASE_RELATIVE_PATH"
    const val CLEANCAP_LISTEN_HOST = "CLEANCAP_LISTEN_HOST"
    const val CLEANCAP_LISTEN_ROUTE = "CLEANCAP_LISTEN_ROUTE"
    const val CLEANCAP_LISTEN_PORT = "CLEANCAP_LISTEN_PORT"
}

suspend fun main() {
    val botToken = System.getenv(EnvVars.CLEANCAP_BOT_TOKEN) ?: return
    val databasePath = System.getenv(EnvVars.CLEANCAP_DATABASE_RELATIVE_PATH) ?: return

    val bot = telegramBot(botToken)
    Database.connect("jdbc:sqlite:$databasePath", driver = "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(ReceiversTable)
        SchemaUtils.create(GroupsTable)
        SchemaUtils.create(UsersTable)
        SchemaUtils.create(AdminsTable)
    }

    val listenHost = System.getenv(EnvVars.CLEANCAP_LISTEN_HOST) ?: "0.0.0.0"
    val listenRoute = System.getenv(EnvVars.CLEANCAP_LISTEN_ROUTE) ?: "/"
    val listenPort = System.getenv(EnvVars.CLEANCAP_LISTEN_PORT)?.toIntOrNull() ?: 8080

    bot.buildBehaviourWithFSM {
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
    }.run {

        start()

        startListenWebhooks(
            listenPort = listenPort,
            engineFactory = Netty,
            exceptionsHandler = { it.printStackTrace() },
            listenHost = listenHost,
            listenRoute = listenRoute,
            block = this.asUpdateReceiver
        )
    }
}