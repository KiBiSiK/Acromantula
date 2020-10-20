package net.cydhra.acromantula.workspace.java

import org.jetbrains.exposed.dao.IntIdTable

class JavaField

internal object JavaFieldTable : IntIdTable("JavaFields") {
    val name = varchar("name", MAX_IDENTIFIER_LENGTH)
    val access = integer("access")

    val owner = reference("owner", JavaClassTable)
    val type = reference("type", JavaIdentifierTable)
}