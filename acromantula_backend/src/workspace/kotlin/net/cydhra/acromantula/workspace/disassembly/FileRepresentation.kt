package net.cydhra.acromantula.workspace.disassembly

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

internal object FileRepresentationTable : IntIdTable() {
    val file = reference("file", FileTable)
    val resource = integer("view")
    val created = datetime("created")
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
     * The workspace resource of this view
     */
    internal var resource by FileRepresentationTable.resource

    /**
     * When the view was created
     */
    var created by FileRepresentationTable.created
        internal set
}