package net.cydhra.acromantula.workspace.filesystem

import org.jetbrains.exposed.dao.id.IntIdTable

const val MAX_FILE_NAME = Short.MAX_VALUE.toInt()

internal object ArchiveTable : IntIdTable() {
    val typeIdent = varchar("archive_type", 255)
}