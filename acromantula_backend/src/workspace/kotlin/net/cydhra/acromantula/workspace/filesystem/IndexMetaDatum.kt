package net.cydhra.acromantula.workspace.filesystem

class IndexMetaDatum

internal object IndexMetaDatumTable : org.jetbrains.exposed.dao.id.IntIdTable("TreeIndexData") {
    val name = varchar("name", 255)
    val value = varchar("value", 255)

    val referencedFile = reference("file", FileTable).nullable()
}