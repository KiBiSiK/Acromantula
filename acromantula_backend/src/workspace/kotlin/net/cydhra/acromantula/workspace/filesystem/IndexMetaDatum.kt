package net.cydhra.acromantula.workspace.filesystem

import org.jetbrains.exposed.dao.IntIdTable

class IndexMetaDatum

internal object IndexMetaDatumTable : IntIdTable("TreeIndexData") {
    val name = varchar("name", 255)
    val value = varchar("value", 255)

    val referencedFile = reference("file", FileTable).nullable()
}