package net.cydhra.acromantula.database

import org.jetbrains.exposed.dao.IntIdTable

class FileEntity

object FileTable : IntIdTable("TreeFile") {
    val name = varchar("name", MAX_FILE_NAME)
    val parent = reference("parent", DirectoryTable).nullable()
    val archive = reference("archive", ArchiveTable).nullable()
    val type = varchar("type", 31).nullable()
}