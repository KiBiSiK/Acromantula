package net.cydhra.acromantula.features.archives

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.util.Either
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * This feature provides a way to express capabilities of imported archive formats. Since not every archive can
 * accept new files or handle deletion of files, all basic file operations are routed through this feature, which
 * will search the modified file's archive and callback to the archive's implementation to ask if the operation is
 * permitted. This does also include operations performed by other features, such as importing new files into an
 * existing archive tree.
 */
object ArchiveFeature {

    private val registeredArchiveTypes = mutableListOf<ArchiveType>()

    /**
     * Register a new archive type implementation
     */
    fun registerArchiveType(type: ArchiveType) {
        registeredArchiveTypes += type
        WorkspaceService.registerArchiveType(type.fileTypeIdentifier)
    }

    /**
     * Whether a file can be added in the given directory
     *
     * @param directory parent directory or null, if file is being added at w
     */
    fun canAddFile(directory: FileEntity?): Boolean {
        if (directory == null) return true
        if (directory.canAddFile.isPresent) return directory.canAddFile.get();

        val archiveRootPair = findArchiveRoot(directory)
        if (archiveRootPair == null) {
            directory.canAddFile = Optional.of(true)
            return true
        } else {
            val type = archiveRootPair.second
            val canAddFile = type.canAddFile()
            directory.canAddFile = Optional.of(canAddFile)
            return canAddFile
        }
    }

    /**
     * Create a file in [parent] directory (or workspace root if null) with given name and content. Throws
     * [IllegalArgumentException] if the containing archive does not support adding files
     */
    fun addFile(fileName: String, parent: FileEntity?, content: ByteArray): FileEntity {
        require(canAddFile(parent)) { "${getArchiveType(parent)} archive does not support adding files" }
        val newFile = WorkspaceService.addFileEntry(fileName, parent, content)
        findArchiveRoot(parent)?.also { (archive, type) -> type.onFileAdded(archive, newFile) }
        return newFile
    }

    /**
     * Whether a file can be moved between two directories
     *
     * @param fromDirectory the move source directory or null if it is the workspace root
     * @param toDirectory the move target directory or null if it is the workspace root
     * @param isDirectory true, if the moved file is a directory (with potentially more children)
     *
     * @return true if all affected file trees support the file movement
     */
    fun canMoveFile(fromDirectory: FileEntity?, toDirectory: FileEntity?, isDirectory: Boolean = false): Boolean {
        val from = if (fromDirectory != null) findArchiveRoot(fromDirectory) else null
        val to = if (toDirectory != null) findArchiveRoot(toDirectory) else null

        return if (from == null && to == null) {
            true
        } else if (from == null) {
            to!!.second.canMoveFile() && (!isDirectory || to.second.canAddDirectory())
        } else if (to == null) {
            from.second.canMoveFile()
        } else if (from == to) {
            from.second.canMoveFile() // to does support directories if the source does as well
        } else { // first and second are different archive trees
            from.second.canDeleteFile() && to.second.canAddFile() && (!isDirectory || to.second.canAddDirectory())
        }
    }

    fun moveFile(file: FileEntity, targetDirectory: FileEntity?) {
        require(targetDirectory?.isDirectory ?: true) { "target must be a directory or null" }
        val parent = transaction {
            val parent = file.parent
            require(canMoveFile(parent, targetDirectory)) { "source or target does not allow moving files" }
            parent
        }

        // move file and update affected archives
        if (parent == targetDirectory) {
            WorkspaceService.moveFileEntry(file, targetDirectory)

            if (parent != null) {
                findArchiveRoot(parent)?.also { (archive, type) -> type.onFileMoved(archive, parent, file) }
            }
        } else {
            findArchiveRoot(parent)?.also { (archive, type) -> type.onFileDelete(archive, file) }
            WorkspaceService.moveFileEntry(file, targetDirectory)
            findArchiveRoot(targetDirectory)?.also { (archive, type) -> type.onFileAdded(archive, file) }
        }
    }

    fun canRenameFile(file: FileEntity, name: String): Boolean {
        val (_, type) = findArchiveRoot(file) ?: return true
        return type.canRenameFile(name)
    }

    fun renameFile(file: FileEntity, newName: String) {
        require(canRenameFile(file, newName)) {
            "${getArchiveType(file)} archive does not support renaming or the given name is invalid for archive format"
        }
        findArchiveRoot(file)?.also { (archive, type) -> type.onFileRename(archive, file, newName) }
        WorkspaceService.renameFileEntry(file, newName)
    }

    /**
     * Whether a file can be deleted from the given directory
     *
     * @param directory a directory in the workspace file tree or null, if the file is being deleted from workspace root
     */
    fun canDeleteFile(directory: FileEntity?): Boolean {
        if (directory == null) return true

        val (_, type) = findArchiveRoot(directory) ?: return true
        return type.canDeleteFile()
    }

    /**
     * Delete a file. Throws an [IllegalArgumentException] if the containing archive format does not support deleting
     * files.
     */
    fun deleteFile(file: FileEntity) {
        transaction {
            require(canDeleteFile(file.parent)) {
                "${getArchiveType(file.parent)} archive does not support " +
                        "deleting files"
            }
        }

        findArchiveRoot(file)?.also { (archive, type) -> type.onFileDelete(archive, file) }
        WorkspaceService.deleteFileEntry(file)
    }

    /**
     * Whether a directory can be added under the given directory
     *
     * @param directory a directory in the workspace file tree or null, if the directory is added at workspace root
     */
    fun canAddDirectory(directory: FileEntity?): Boolean {
        if (directory == null) return true

        val (_, type) = findArchiveRoot(directory) ?: return true
        return type.canAddDirectory()
    }

    fun addDirectory(name: String, parent: FileEntity?): FileEntity {
        require(canAddDirectory(parent)) { "${getArchiveType(parent)} archive does not support adding directories" }
        val directory = WorkspaceService.addDirectoryEntry(name, parent)
        findArchiveRoot(parent)?.also { (archive, type) -> type.onDirectoryAdded(archive, directory) }
        return directory
    }

    /**
     * Whether given archive type can be created from scratch. This will allow a user to create a directory and mark
     * is as an archive of this format, so that subsequent actions are subject to the capabilities of this archive type.
     *
     * @param type the archive type identifier as defined by the [ArchiveType] implementation
     */
    fun canCreateArchiveFromScratch(type: String): Boolean {
        return findArchiveType(type).canCreateArchiveFromScratch()
    }

    /**
     * Mark the given (empty) directory as an archive of this type. The directory does not necessarily need to be
     * empty, but if it contains files, they will be checked against the capabilities of the archive type first
     */
    fun createArchiveFromScratch(directory: FileEntity, type: String) {
        if (!directory.isDirectory)
            throw IllegalArgumentException("cannot create archive directory out of files")

        val archiveType = findArchiveType(type)

        if (directory.children.isNotEmpty()) {
            checkCapabilitiesRecursively(directory.children, archiveType) // throws on error
        }

        archiveType.createArchiveFromScratch(directory)
    }

    /**
     * Get the archive type identifier for a given directory, or null if the directory is not in an archive
     */
    fun getArchiveType(directory: FileEntity?): String? {
        return findArchiveRoot(directory)?.second?.fileTypeIdentifier
    }

    /**
     * Mark an existing directory as an archive. This is only meant for use during import. Use
     * [createArchiveFromScratch] to mark directories as an archive at a later point. This method does not perform
     * any sanity checks on the directory content
     */
    fun markDirectoryAsArchive(directory: FileEntity, archiveType: ArchiveType) {
        WorkspaceService.addArchiveEntry(directory, archiveType.fileTypeIdentifier)
    }

    /**
     * Checks whether a file tree is applicable to become an archive of the given type
     *
     * @param subFiles all files that will be at root-level in the new archive
     * @param type archive type to be created from the directory
     */
    private fun checkCapabilitiesRecursively(subFiles: List<FileEntity>, type: ArchiveType) {
        for (file in subFiles) {
            if (file.isDirectory) {
                if (!type.canAddDirectory())
                    throw IllegalStateException("cannot create ${type.fileTypeIdentifier} archive with sub-directories")

                checkCapabilitiesRecursively(file.children, type)
            } else {
                if (!type.canAddFile()) {
                    throw IllegalStateException("cannot create ${type.fileTypeIdentifier} archive containing existing files")
                }
            }
        }
    }

    /**
     * Find the archive the given file belongs to. If the file does not belong to any archive, null is returned
     */
    private fun findArchiveRoot(file: FileEntity?): Pair<FileEntity, ArchiveType>? {
        // if we are at workspace root, we are not in an archive
        if (file == null) return null
        if (file.archiveRoot.isPresent) return when (val root = file.archiveRoot.get()) {
            is Either.Right -> null
            is Either.Left -> file to findArchiveType(root.value)
        }

        val archiveType = if (file.archiveType != null) {
            file.archiveType
        } else {
            findArchiveRoot(file.parent)?.second?.fileTypeIdentifier
        }

        return if (archiveType != null) {
            file.archiveRoot = Optional.of(Either.Left(archiveType))
            file to findArchiveType(archiveType)
        } else {
            file.archiveRoot = Optional.of(Either.Right(FileEntity.Empty))
            null
        }
    }

    /**
     * Find the [ArchiveType] implementation with the given [type] as [ArchiveType.fileTypeIdentifier]. Throws an
     * [IllegalArgumentException] if no such type exists. If [type] is null or empty, [ZipArchiveType] will be returned
     */
    private fun findArchiveType(type: String?): ArchiveType {
        if (type.isNullOrEmpty()) return ZipArchiveType

        return registeredArchiveTypes.first { it.fileTypeIdentifier == type }
    }
}