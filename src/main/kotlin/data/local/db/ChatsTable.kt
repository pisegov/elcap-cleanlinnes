package data.local.db

import domain.model.Chat
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

object ChatsTable : Table("chats") {
    private val id = integer("chat_id").autoIncrement()
    val telegramChatId = long("telegram_chat_id").references(ReceiversTable.telegramChatId).uniqueIndex()
    val title = varchar("title", 100)
    override val primaryKey = PrimaryKey(id, name = "chat_id")

    private val table = this

    init {
        uniqueIndex(telegramChatId)
    }

    fun insert(chat: Chat) {
        return transaction {
            upsert {
                it[title] = chat.title
                it[telegramChatId] = chat.telegramChatId
            }
        }
    }

    fun getAllChats(): List<ChatDTO> {
        return transaction {
            val listModel = table.selectAll()

            listModel.toList().map { model ->
                ChatDTO(
                    id = model[table.id],
                    telegramChatId = model[table.telegramChatId],
                    title = model[table.title],
                )
            }
        }
    }

    fun removeChat(telegramChatId: Long) {
        transaction {
            table.deleteWhere { table.telegramChatId.eq(telegramChatId) }
        }
    }
}
