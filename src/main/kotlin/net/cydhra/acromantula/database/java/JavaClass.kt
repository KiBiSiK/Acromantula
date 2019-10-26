package net.cydhra.acromantula.database.java

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object JavaClassTable : IntIdTable("JavaClasses") {
    val identifier = reference("identifier", JavaIdentifierTable, ReferenceOption.RESTRICT).index()
    val name = varchar("name", MAX_IDENTIFIER_LENGTH)
    val superIdentifier = reference("super_identifier", JavaIdentifierTable).nullable()
    val module = reference("module", JavaModuleTable).nullable()
    val sourceFile = reference("sourcefile", JavaSourceFileTable).nullable()

    val access = integer("access")
    val signature = varchar("signature", MAX_IDENTIFIER_LENGTH)
}

class JavaClass
