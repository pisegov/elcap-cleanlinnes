import data.local.admin.AdminsTable
import data.local.group.GroupsTable
import data.local.receiver.ReceiversTable
import data.local.user.UsersTable
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.api.webhook.setWebhookInfo
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithFSMAndStartLongPolling
import dev.inmo.tgbotapi.extensions.utils.updates.flowsUpdatesFilter
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.setWebhookInfoAndStartListenWebhooks
import dev.inmo.tgbotapi.requests.webhook.SetWebhook
import dev.inmo.tgbotapi.types.BotCommand
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ioc.DaggerApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


suspend fun main() {
    val bot = telegramBot(BOT_TOKEN)
    Database.connect("jdbc:sqlite:data.db", driver = "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(ReceiversTable)
        SchemaUtils.create(GroupsTable)
        SchemaUtils.create(UsersTable)
        SchemaUtils.create(AdminsTable)
    }

    bot.buildBehaviourWithFSM{
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
    }
    bot.setWebhookInfo("")


    val scope = CoroutineScope(Dispatchers.Default)

    val filter = flowsUpdatesFilter {
        // ...
    }

    val environment = applicationEngineEnvironment {
        module {
            routing {
                includeWebhookHandlingInRoute(
                    scope,
                    {
                        it.printStackTrace()
                    },
                    filter.asUpdateReceiver
                )
            }
        }
        connector {
            host = "0.0.0.0"
            port = 8080
        }
    }

    embeddedServer(CIO, environment).start(true) // will start server and wait its stoping

    bot.setWebhookInfoAndStartListenWebhooks(
        listenPort = 8080,
        engineFactory = Netty,
        setWebhookRequest = SetWebhook(
            url = "",
            certificateFile = TODO(),
            ipAddress = TODO(),
            maxAllowedConnections = TODO(),
            allowedUpdates = TODO(),
            dropPendingUpdates = TODO(),
            secretToken = TODO(),
            serializationConstructorMarker = TODO()
        ),
        exceptionsHandler = TODO(),
        listenHost = TODO(),
        listenRoute = TODO(),
        privateKeyConfig = TODO(),
        scope = TODO(),
        mediaGroupsDebounceTimeMillis = TODO(),
        additionalApplicationEnvironmentConfigurator = TODO(),
        additionalEngineConfigurator = TODO(),
        block = TODO()
    )

}