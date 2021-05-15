package net.cydhra.acromantula.workspace.filesystem

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

const val MAX_FILE_NAME = Short.MAX_VALUE.toInt()

internal object ArchiveTable : org.jetbrains.exposed.dao.id.IntIdTable("TreeArchives") {

}

class ArchiveEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ArchiveEntity>(ArchiveTable)
}