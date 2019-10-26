package net.cydhra.acromantula.database.java

import org.jetbrains.exposed.dao.IntIdTable

class JavaSourceFile

object JavaSourceFileTable : IntIdTable("JavaSourceFiles") {
    val path = varchar("path", MAX_IDENTIFIER_LENGTH)
}