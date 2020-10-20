package net.cydhra.acromantula.workspace.filesystem

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

internal object FileTable : IntIdTable("TreeFile") {
    val name = varchar("name", MAX_FILE_NAME)
    val parent = reference("parent", DirectoryTable).nullable()
    val type = varchar("type", 31).nullable()
    val resource = integer("resource").nullable()
}

/**
 * A singular file entity
 */
class FileEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FileEntity>(FileTable)

    var name by FileTable.name
        internal set
    var parent by DirectoryEntity optionalReferencedOn FileTable.parent
        internal set
    var type by FileTable.type
        internal set
    var resource by FileTable.resource
        internal set
}