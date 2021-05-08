package net.cydhra.acromantula.workspace.filesystem

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

// This table is NOT private, so external models can reference files.
object FileTable : IntIdTable("TreeFile") {
    val name = varchar("name", MAX_FILE_NAME)
    val parent = reference("parent", FileTable).nullable()
    val isDirectory = bool("is_directory").default(false)
    val type = varchar("type", 31).nullable()
    val resource = integer("resource").nullable()
    val archive = reference("archive", ArchiveTable).nullable()

    init {
        check { archive.isNull() or (archive.isNotNull() and (isDirectory eq true)) }
    }
}

/**
 * A singular file entity
 */
class FileEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FileEntity>(FileTable)

    var name by FileTable.name
        internal set
    var parent by FileEntity optionalReferencedOn FileTable.parent
        internal set
    var isDirectory by FileTable.isDirectory
        internal set
    var type by FileTable.type
        internal set
    var resource by FileTable.resource
        internal set
    var archiveEntity by ArchiveEntity optionalReferencedOn FileTable.archive
}