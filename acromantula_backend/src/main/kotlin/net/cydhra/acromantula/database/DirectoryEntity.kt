package net.cydhra.acromantula.database

import org.jetbrains.exposed.dao.IntIdTable

class DirectoryEntity

object DirectoryTable : IntIdTable("TreeDirectory") {
    val name = varchar("name", MAX_FILE_NAME)
    val parent = reference("parent", DirectoryTable).nullable()
    val archive = reference("archive", ArchiveTable).nullable()
}