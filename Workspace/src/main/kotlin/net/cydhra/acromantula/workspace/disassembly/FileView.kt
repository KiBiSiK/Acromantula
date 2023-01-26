package net.cydhra.acromantula.workspace.disassembly

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileTable
import org.jetbrains.exposed.sql.jodatime.datetime
import java.time.Instant

/**
 * A table that holds information about all generated file views. A file view is a transformed file in the workspace
 * that holds a different content type to represent the same, or a subset of information. For example, a bitmap
 * containing the same image data as a proprietary image format is a representation of the proprietary file.
 */
internal object FileViewTable : org.jetbrains.exposed.dao.id.IntIdTable() {
    val file = reference("file", FileTable)
    val type = varchar("type", 255)
    val resource = integer("view")
    val created = datetime("created")

    init {
        uniqueIndex(file, type)
    }
}

/**
 * A human-readable view of a file, which is itself stored as a resource in workspace.
 */
class FileView(val file: FileEntity, val type: String, val resource: Int, val created: Instant)