package net.cydhra.acromantula.database

import org.jetbrains.exposed.dao.IntIdTable

const val MAX_FILE_NAME = 255

class Archive

object ArchiveTable : IntIdTable("TreeArchives") {
    val name = varchar("name", MAX_FILE_NAME)
}