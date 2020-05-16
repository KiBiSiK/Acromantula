package net.cydhra.acromantula.data.java

import org.jetbrains.exposed.dao.IntIdTable

class JavaMethod

object JavaMethodTable : IntIdTable("JavaMethods") {
    val identifier = reference("identifier", JavaIdentifierTable)
    val owner = reference("owner", JavaClassTable)
    val name = varchar("name", MAX_IDENTIFIER_LENGTH)
    val descriptor = varchar("descriptor", MAX_IDENTIFIER_LENGTH)
}