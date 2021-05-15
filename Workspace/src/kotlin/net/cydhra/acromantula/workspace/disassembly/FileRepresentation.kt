package net.cydhra.acromantula.workspace.disassembly

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.datetime

internal object FileRepresentationTable : org.jetbrains.exposed.dao.id.IntIdTable() {
    val file = reference("file", FileTable)
    val type = varchar("type", 255)
    val resource = integer("view")
    val created = datetime("created")

    init {
        uniqueIndex(file, type)
    }
}

/**
 * A human-readable representation of any file
 */
class FileRepresentation(entityID: EntityID<Int>) : IntEntity(entityID) {
    companion object : IntEntityClass<FileRepresentation>(FileRepresentationTable)

    /**
     * The source file of this representation
     */
    var file by FileEntity referencedOn FileRepresentationTable.file
        internal set

    /**
     * Representation type to differ between different representations generated for the same file
     */
    var type by FileRepresentationTable.type

    /**
     * The workspace resource of this view
     */
    var resource by FileRepresentationTable.resource

    /**
     * When the view was created
     */
    var created by FileRepresentationTable.created
        internal set
}