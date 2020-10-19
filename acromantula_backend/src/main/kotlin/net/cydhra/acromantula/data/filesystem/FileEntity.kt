package net.cydhra.acromantula.data.filesystem

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object FileTable : IntIdTable("TreeFile") {
    val name = varchar("name", MAX_FILE_NAME)
    val parent = reference("parent", DirectoryTable).nullable()
    val archive = reference("archive", ArchiveTable).nullable()
    val type = varchar("type", 31).nullable()
    val resource = integer("resource").nullable()
}

/**
 * A singular file entity
 */
class FileEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FileEntity>(FileTable)

    var name by FileTable.name
    var parent by DirectoryEntity optionalReferencedOn FileTable.parent
    var archive by ArchiveEntity optionalReferencedOn FileTable.archive
    var type by FileTable.type
    var resource by FileTable.resource
}