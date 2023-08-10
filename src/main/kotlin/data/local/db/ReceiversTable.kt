package data.local.db

import domain.model.Chat
import domain.model.Receiver
import domain.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object ReceiversTable : Table("receivers") {
    val telegramChatId = long("telegram_chat_id").uniqueIndex()

    private val table = this

    fun insert(chatId: Long) {
        return transaction {
            insert {
                it[telegramChatId] = chatId
            }
        }
    }

    fun getAllReceivers(): List<Receiver> {
        return transaction {
            val usersList = table.join(UsersTable, JoinType.INNER).selectAll().toList().map {
                User(
                    telegramChatId = it[telegramChatId],
                    name = it[UsersTable.name],
                    username = it[UsersTable.username]
                )
            }
            val chatsList = table.join(ChatsTable, JoinType.INNER).selectAll().toList().map {
                Chat(
                    telegramChatId = it[telegramChatId],
                    title = it[ChatsTable.title]
                )
            }

            (usersList + chatsList)
        }
    }

    fun removeReceiver(telegramChatId: Long) {
        transaction {
            table.deleteWhere { table.telegramChatId.eq(telegramChatId) }
        }
    }
}

