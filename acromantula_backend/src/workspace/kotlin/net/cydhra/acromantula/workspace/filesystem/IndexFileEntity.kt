package net.cydhra.acromantula.workspace.filesystem

import org.jetbrains.exposed.dao.IntIdTable

class IndexFileEntity

object IndexFileTable : IntIdTable("TreeIndexFile") {
    val name = varchar("name", MAX_FILE_NAME)
    val parent = reference("parent", DirectoryTable).nullable()
    val archive = reference("archive", ArchiveTable).nullable()
}