package net.cydhra.acromantula.data

import org.jetbrains.exposed.dao.IntIdTable

class IndexMetaDatum

object IndexMetaDatumTable : IntIdTable("TreeIndexData") {
    val name = varchar("name", 255)
    val value = varchar("value", 255)

    val indexFile = reference("index", IndexFileTable)
    val referencedFile = reference("file", FileTable).nullable()
}