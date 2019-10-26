package net.cydhra.acromantula.database.java

import org.jetbrains.exposed.dao.IntIdTable

class JavaParameter

object JavaParameterTable : IntIdTable("JavaParameters") {
    val name = varchar("name", MAX_IDENTIFIER_LENGTH)
    val method = reference("method", JavaMethodTable)
    val type = reference("type", JavaIdentifierTable)
}