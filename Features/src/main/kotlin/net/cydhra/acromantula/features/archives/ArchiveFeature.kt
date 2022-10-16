package net.cydhra.acromantula.features.archives

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.util.TreeNode
import org.jetbrains.exposed.sql.transactions.transaction

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
    }

    /**
     * Whether a file can be added in the given directory
     *
     * @param directory parent directory or null, if file is being added at w
     */
    fun canAddFile(directory: FileEntity?): Boolean {
        if (directory == null) return true

        val (_, type) = findArchiveRoot(directory) ?: return true
        return type.canAddFile()
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
     * Whether a directory can be added under the given directory
     *
     * @param directory a directory in the workspace file tree or null, if the directory is added at workspace root
     */
    fun canAddDirectory(directory: FileEntity?): Boolean {
        if (directory == null) return true

        val (_, type) = findArchiveRoot(directory) ?: return true
        return type.canAddDirectory()
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
        val fileTree = WorkspaceService.listFilesRecursively(directory)
        assert(fileTree.size == 1)
        val subFileTree = fileTree[0]

        assert(subFileTree.value == directory)
        if (subFileTree.childList.isNotEmpty()) {
            checkCapabilitiesRecursively(subFileTree.childList, archiveType) // throws on error
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
     * Checks whether a file tree is applicable to become an archive of the given type
     *
     * @param subFiles all files that will be at root-level in the new archive
     * @param type archive type to be created from the directory
     */
    private fun checkCapabilitiesRecursively(subFiles: List<TreeNode<FileEntity>>, type: ArchiveType) {
        for (file in subFiles) {
            if (file.value.isDirectory) {
                if (!type.canAddDirectory())
                    throw IllegalStateException("cannot create ${type.fileTypeIdentifier} archive with sub-directories")

                checkCapabilitiesRecursively(file.childList, type)
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

        return transaction {
            if (file.archiveEntity != null) {
                file to findArchiveType(file.archiveEntity!!.typeIdent)
            } else {
                findArchiveRoot(file.parent)
            }
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