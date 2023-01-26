package net.cydhra.acromantula.workspace.filesystem

import net.cydhra.acromantula.workspace.disassembly.FileView
import net.cydhra.acromantula.workspace.util.Either
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import java.util.*

// This table is NOT private, so external models can reference files.
object FileTable : IntIdTable("TreeFile") {
    val name = varchar("name", MAX_FILE_NAME)
    val parent = reference("parent", FileTable).nullable()
    val isDirectory = bool("is_directory").default(false)
    val type = varchar("type", 31).nullable()
    val resource = integer("resource").nullable()
    val archive = reference("archive", ArchiveTable).nullable()

    init {
        check { archive.isNull() or (archive.isNotNull() and (isDirectory eq true)) }
    }
}

/**
 * A singular file entity
 */
class FileEntity internal constructor(
    name: String,
    parent: FileEntity?,
    isDirectory: Boolean,
    type: String,
    archiveType: String?,
    internal val resource: Int
) {

    /**
     * Marker type for intentionally empty optionals
     */
    object Empty

    /**
     * Mutable list of child files
     */
    internal val childEntities = mutableListOf<FileEntity>()

    /**
     * A list of all files within this directory. If this file is not a directory, this list is empty
     */
    val children: List<FileEntity> = childEntities

    /**
     * Mutable list of file views associated with this file
     */
    internal val viewEntities = mutableListOf<FileView>()

    /**
     * A list of all [FileView] entities associated with this file. If this file is a directory, this list is empty
     */
    val views: List<FileView> = viewEntities

    var name: String = name
        internal set
    var parent: FileEntity? = parent
        internal set
    var isDirectory: Boolean = isDirectory
        internal set
    var type: String = type
        internal set

    /**
     * Archive type identifier
     */
    var archiveType: String? = archiveType
        internal set

    /**
     * Internal cache for database id, which can be used by database sync to quickly reference this file. -1 if no
     * value is cached.
     */
    internal var databaseId: EntityID<Int>? = null

    /**
     * Whether this file supports adding child files. This optional is initialized empty, because the property is
     * only used for caching. Property utilized by archive feature
     */
    var canAddFile: Optional<Boolean> = Optional.empty()

    /**
     * The archive root directory this file belongs to.  This optional is initialized empty, because the property is
     * only used for caching. Property utilized by archive feature
     */
    var archiveRoot: Optional<Either<String, Empty>> = Optional.empty()
}