package net.cydhra.acromantula.workspace.java

import net.cydhra.acromantula.workspace.filesystem.FileTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object JavaClassTable : IntIdTable("JavaClasses") {
    val identifier = reference("identifier", JavaIdentifierTable, ReferenceOption.RESTRICT).index()
    val name = varchar("name", MAX_IDENTIFIER_LENGTH)
    val superIdentifier = reference("super_identifier", JavaIdentifierTable).nullable()
    val module = reference("module", JavaModuleTable).nullable()
    val sourceFile = reference("sourcefile", JavaSourceFileTable).nullable()
    val classFile = reference("classfile", FileTable)

    val access = integer("access")
    val signature = varchar("signature", MAX_IDENTIFIER_LENGTH)
}

/**
 * A java class entity object for manipulating java classes from algorithms
 */
class JavaClass(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<JavaClass>(JavaClassTable)

    val name by JavaClassTable.name
    val accessFlags by JavaClassTable.access
    val signature by JavaClassTable.signature
}

/**
 * Import a java class into database
 */
fun JavaClass.import(byteCode: ByteArray): JavaClass {

    TODO()
}
