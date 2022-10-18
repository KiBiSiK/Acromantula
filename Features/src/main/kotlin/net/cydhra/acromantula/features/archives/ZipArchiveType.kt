package net.cydhra.acromantula.features.archives

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * Default zip archive type with no constraints
 */
object ZipArchiveType : ArchiveType {
    override val fileTypeIdentifier: String = ""

    override fun canAddFile(): Boolean = true

    override fun onFileAdded(archive: FileEntity, fileEntity: FileEntity) {}

    override fun canMoveFile(): Boolean = true

    override fun onFileMoved(archive: FileEntity, source: FileEntity, file: FileEntity) {}

    override fun canRenameFile(name: String) = true

    override fun onFileRename(archive: FileEntity, file: FileEntity, newName: String) {}

    override fun canDeleteFile(): Boolean = true

    override fun onFileDelete(archive: FileEntity, file: FileEntity) {}

    override fun canAddDirectory(): Boolean = true

    override fun onDirectoryAdded(archive: FileEntity, directory: FileEntity) {}

    override fun canCreateArchiveFromScratch(): Boolean = true

    override fun createArchiveFromScratch(directory: FileEntity) {
        WorkspaceService.addArchiveEntry(directory, this.fileTypeIdentifier)
    }
}