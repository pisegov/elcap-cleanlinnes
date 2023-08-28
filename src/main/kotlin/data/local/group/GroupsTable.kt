package data.local.group

import domain.model.Group
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

object GroupsTable : Table("chats") {
    private val id = integer("chat_id").autoIncrement()
    val telegramChatId = long("telegram_chat_id")
    val title = varchar("title", 100).nullable()
    override val primaryKey = PrimaryKey(id, name = "chat_id")

    private val table = this

    init {
        uniqueIndex(telegramChatId)
    }

    fun insert(group: Group) {
        return transaction {
            upsert {
                it[title] = group.chatTitle
                it[telegramChatId] = group.telegramChatId
            }
        }
    }

    fun getAllGroups(): List<GroupDTO> {
        return transaction {
            val listModel = table.selectAll()

            listModel.toList().map { model ->
                GroupDTO(
                    id = model[GroupsTable.id],
                    telegramChatId = model[telegramChatId],
                    title = model[title] ?: "",
                )
            }
        }
    }

    fun removeGroup(telegramChatId: Long) {
        transaction {
            table.deleteWhere { GroupsTable.telegramChatId.eq(telegramChatId) }
        }
    }
}