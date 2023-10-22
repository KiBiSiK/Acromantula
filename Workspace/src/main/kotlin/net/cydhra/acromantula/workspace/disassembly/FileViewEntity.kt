package net.cydhra.acromantula.workspace.disassembly

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.datetime
import java.time.Instant

/**
 * A table that holds information about all generated file views. A file view is a transformed file in the workspace
 * that holds a different content type to represent the same, or a subset of information. For example, a bitmap
 * containing the same image data as a proprietary image format is a representation of the proprietary file.
 */
internal object FileViewTable : org.jetbrains.exposed.dao.id.IntIdTable() {
    val file = reference("file", FileTable)
    val viewGenerator = varchar("type", 255)
    val mediaType = enumeration("media", MediaType::class)
    val resource = integer("view")
    val created = datetime("created")

    init {
        uniqueIndex(file, viewGenerator)
    }
}

enum class MediaType(val fileExtension: String) {
    TXT("txt"), PNG("png"), WAV("wav"), HTML("html")
}

/**
 * A human-readable view of a file, which is itself stored as a resource in workspace.
 */
class FileViewEntity(val file: FileEntity, val type: String, val mediaType: MediaType, val resource: Int, val
created: Instant) {
    lateinit var databaseId: EntityID<Int>
}