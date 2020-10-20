package net.cydhra.acromantula.workspace.filesystem

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object DirectoryTable : IntIdTable("TreeDirectory") {
    val name = varchar("name", MAX_FILE_NAME)
    val parent = reference("parent", DirectoryTable).nullable()
    val archive = reference("archive", ArchiveTable).nullable()
}

class DirectoryEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DirectoryEntity>(DirectoryTable)

    var name by DirectoryTable.name
    var parent by DirectoryEntity optionalReferencedOn DirectoryTable.parent
    var archive by ArchiveEntity optionalReferencedOn DirectoryTable.archive
}