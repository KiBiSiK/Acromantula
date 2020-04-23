package net.cydhra.acromantula.database.java

import org.jetbrains.exposed.dao.IntIdTable

class JavaModule

object JavaModuleTable : IntIdTable("JavaModule") {
    val name = varchar("name", MAX_IDENTIFIER_LENGTH)
}