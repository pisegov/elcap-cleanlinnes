import data.ChatsRepositoryImpl
import data.ReceiversRepositoryImpl
import data.UsersRepositoryImpl
import data.local.*
import data.local.in_memory.InMemoryChatsDataSource
import data.local.in_memory.InMemoryDataSourceImpl
import data.local.in_memory.InMemoryUsersDataSource
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.forwardMessage
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.api.send.withTypingAction
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.*
import dev.inmo.tgbotapi.extensions.utils.shortcuts.executeUnsafe
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestGroupButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestUserButton
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.chat.ExtendedUser
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.PossiblyForwardedMessage
import dev.inmo.tgbotapi.types.request.RequestId
import dev.inmo.tgbotapi.utils.extensions.threadIdOrNull
import dev.inmo.tgbotapi.utils.row
import domain.ChatsRepository
import domain.ReceiversRepository
import domain.UsersRepository
import domain.model.Chat
import util.BOT_TOKEN

suspend fun main() {
    val bot = telegramBot(BOT_TOKEN)

    val usersDataSource: UsersDataSource = InMemoryUsersDataSource()
    val chatsDataSource: ChatsDataSource = InMemoryChatsDataSource()
    val usersRepository: UsersRepository = UsersRepositoryImpl(usersDataSource)
    val chatsRepository: ChatsRepository = ChatsRepositoryImpl(chatsDataSource)
    val receiversRepository: ReceiversRepository =
        ReceiversRepositoryImpl(InMemoryDataSourceImpl(usersDataSource, chatsDataSource))
    bot.buildBehaviourWithLongPolling {
        println(getMe())

        onCommand("start", initialFilter = { it.chat is PrivateChat }) {
            send(it.chat, "Start message")
        }
        onCommand("help", initialFilter = { it.chat is PrivateChat }) {
            send(it.chat, "Help message")
        }


//        onText {
//            send(it.chat, "Сорри, я не пересылаю обычный текст, введите команду /call")
//        }

        onSticker {
            reply(it, "Спасибо за стикер)")
        }

        onContentMessage {
            println(it.chat)
        }

        onNewChatMembers {
            println(it.chatEvent)
            println(it.chat)
            val chat = it.chat as GroupChat
            chatsRepository.addChat(
                Chat(
                    telegramChatId = chat.id.chatId,
                    title = chat.title,
                )
            )

        }

        onLeftChatMember {
            println(it.chatEvent)
            println(it.chat)
            chatsRepository.removeChat(it.chat.id.chatId)
        }

        onEditedPhoto {
            println(it.messageId)
        }

        onPhoto { it ->
            println(it.messageId)
            val receivers = receiversRepository.getReceiversList()
            receivers.forEach { chat ->
                withTypingAction(it.chat) {
                    if (it.forwardable) {
                        val message: PossiblyForwardedMessage = it
                        message.apply {
                            val new = forwardMessage(
                                ChatId(chat.telegramChatId),
                                it,
                                threadId = it.threadIdOrNull
                            )
                            println(new)
                        }
                    } else {
                        executeUnsafe(
                            it.content.createResend(
                                ChatId(chat.telegramChatId),
                                messageThreadId = it.threadIdOrNull,
                                allowSendingWithoutReply = false
                            )
                        ) { errorsList ->
                            errorsList.forEach { println(it) }
                        }
                    }
                }
            }
        }

        val requestIdUserAny = RequestId(2)
        val requestIdGroup = RequestId(12)
        val keyboard: ReplyKeyboardMarkup = replyKeyboard(
            resizeKeyboard = true,
            oneTimeKeyboard = true,
        ) {
            row {
                requestUserButton("Добавить пользователя ", requestIdUserAny)
                requestGroupButton("Добавить группу", requestIdGroup)
            }
        }

        onCommand("add_receiver", initialFilter = { it.chat is PrivateChat }) {
            reply(
                it,
                "Here possible requests buttons:",
                replyMarkup = keyboard,
            )
        }

        onCommand("show_receivers", initialFilter = { it.chat is PrivateChat }) {
            reply(
                it,
                receiversRepository.getReceiversList().toString(),
            )
        }
        onUserShared {
            receiversRepository.addReceiver(it.chatEvent.userId.chatId)

            val replyStringBuilder = StringBuilder("Получатель ")
            try {
                val userId = it.chatEvent.userId
                val user = getChat(userId) as ExtendedUser
                replyStringBuilder.append("${user.firstName} ${user.lastName}".trim())
                replyStringBuilder.append(" сохранён :)\nБот готов пересылать пользователю запросы")
            } catch (e: Throwable) {
                replyStringBuilder.append("сохранён :)\nПользователь ещё не активировал чат с ботом\nКак только он это сделает, бот сможет пересылать запросы")
            }

            reply(
                it,
                replyStringBuilder.toString(),
                replyMarkup = ReplyKeyboardRemove()
            )
        }
        onChatShared {
            reply(
                it,
                "Получатель сохранён :)\nНапоминаю, чтобы бот мог пересылать запросы в группу, он должен быть добавлен в эту группу",
                replyMarkup = ReplyKeyboardRemove()
            )
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