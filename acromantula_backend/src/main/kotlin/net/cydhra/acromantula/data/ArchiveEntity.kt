package net.cydhra.acromantula.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

const val MAX_FILE_NAME = 255

object ArchiveTable : IntIdTable("TreeArchives") {
    val name = varchar("name", MAX_FILE_NAME)
}

class ArchiveEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ArchiveEntity>(ArchiveTable)
}