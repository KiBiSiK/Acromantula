package net.cydhra.acromantula.features.archives

import net.cydhra.acromantula.workspace.filesystem.ArchiveEntity
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Default zip archive type with no constraints
 */
object ZipArchiveType : ArchiveType {
    override val fileTypeIdentifier: String = ""

    override fun canAddFile(): Boolean = true

    override fun canMoveFile(): Boolean = true

    override fun canDeleteFile(): Boolean = true

    override fun canAddDirectory(): Boolean = true

    override fun canAddSubArchive(): Boolean = true

    override fun canCreateArchiveFromScratch(): Boolean = true

    override fun createArchiveFromScratch(directory: FileEntity) {
        transaction {
            directory.archiveEntity = ArchiveEntity.new {
                typeIdent = ""
            }
        }
    }
}