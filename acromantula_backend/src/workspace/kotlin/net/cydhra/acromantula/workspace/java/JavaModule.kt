package net.cydhra.acromantula.workspace.java

import org.jetbrains.exposed.dao.IntIdTable

class JavaModule

internal object JavaModuleTable : IntIdTable("JavaModule") {
    val name = varchar("name", MAX_IDENTIFIER_LENGTH)
}