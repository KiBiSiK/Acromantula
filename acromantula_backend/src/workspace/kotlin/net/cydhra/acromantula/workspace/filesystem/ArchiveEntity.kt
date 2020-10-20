package net.cydhra.acromantula.workspace.filesystem

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

const val MAX_FILE_NAME = 255

object ArchiveTable : IntIdTable("TreeArchives") {

}

class ArchiveEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ArchiveEntity>(ArchiveTable)
}