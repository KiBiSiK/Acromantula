package net.cydhra.acromantula.workspace.java

import org.jetbrains.exposed.dao.IntIdTable

class JavaSourceFile

internal object JavaSourceFileTable : IntIdTable("JavaSourceFiles") {
    val path = varchar("path", MAX_IDENTIFIER_LENGTH)
}