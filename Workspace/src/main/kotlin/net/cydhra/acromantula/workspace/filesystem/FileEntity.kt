package net.cydhra.acromantula.workspace.filesystem

import net.cydhra.acromantula.workspace.disassembly.FileView
import net.cydhra.acromantula.workspace.util.Either
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
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
    parent: Optional<Int>,
    isDirectory: Boolean,
    type: String,
    archiveEntity: Optional<Int>,
    private val resource: Int
) {

    /**
     * Marker type for intentionally empty optionals
     */
    object Empty

    var name: String = name
        internal set
    var parent: Optional<Int> = parent
        internal set
    var isDirectory: Boolean = isDirectory
        internal set
    var type: String = type
        internal set

    internal var archiveEntity: Optional<Int> = archiveEntity

    var archiveType: Optional<String> = Optional.empty()
        get() {
            return if (!archiveEntity.isPresent) {
                field = Optional.empty()
                field
            } else {
                if (!field.isPresent) {
                    field = Optional.of(ArchiveTable
                        .select { ArchiveTable.id eq archiveEntity.get() }
                        .first()[ArchiveTable.typeIdent])
                }
                field
            }
        }
        private set

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

    /**
     * Get all views associated with this file
     */
    fun getViews(): List<FileView> {
        TODO("not implemented")
    }
}