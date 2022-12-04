package net.cydhra.acromantula.workspace.filesystem

import net.cydhra.acromantula.workspace.disassembly.FileRepresentation
import net.cydhra.acromantula.workspace.disassembly.FileRepresentationTable
import net.cydhra.acromantula.workspace.util.Either
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
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
class FileEntity(id: EntityID<Int>) : IntEntity(id) {

    /**
     * Marker type for intentionally empty optionals
     */
    object Empty

    companion object : IntEntityClass<FileEntity>(FileTable)

    var name by FileTable.name
        internal set
    var parent by FileEntity optionalReferencedOn FileTable.parent
        internal set
    var isDirectory by FileTable.isDirectory
        internal set
    var type by FileTable.type
        internal set

    var resource by FileTable.resource

    var archiveEntity by ArchiveEntity optionalReferencedOn FileTable.archive

    private val views by FileRepresentation referrersOn FileRepresentationTable.file

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
    fun getViews(): List<FileRepresentation> {
        return transaction {
            this@FileEntity.views.toList()
        }
    }
}