package net.cydhra.acromantula.workspace.filesystem

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

const val MAX_FILE_NAME = Short.MAX_VALUE.toInt()

internal object ArchiveTable : IntIdTable() {
    val typeIdent = varchar("archive_type", 255)
}

class ArchiveEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ArchiveEntity>(ArchiveTable)

    /**
     * The archive type identifier that provides an [net.cydhra.acromantula.features.archives.ArchiveType]
     * implementation
     */
    var typeIdent by ArchiveTable.typeIdent
}