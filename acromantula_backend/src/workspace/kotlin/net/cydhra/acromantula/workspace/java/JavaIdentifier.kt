package net.cydhra.acromantula.workspace.java

import org.jetbrains.exposed.dao.IntIdTable

const val MAX_IDENTIFIER_LENGTH: Int = Short.MAX_VALUE.toInt()

internal object JavaIdentifierTable : IntIdTable("JavaIdentifier") {
    val identifier = varchar("identifier", MAX_IDENTIFIER_LENGTH).uniqueIndex()
}

class JavaIdentifier