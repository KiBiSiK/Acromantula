package net.cydhra.acromantula.features.util

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * A utility to create file trees from archive files. It offers methods to retrieve parent directories for file
 * entries from database and it will automatically insert them when necessary.
 *
 * @param archiveFile the archive to build a directory tree
 */
class FileTreeBuilder(archiveFile: FileEntity) {

    companion object {
        private const val LAST_DIRECTORY_PATTERN = "[^\\/]*\\/\$"
        private val lastDirectoryRegex = LAST_DIRECTORY_PATTERN.toRegex()
    }

    private val directoryMap = mutableMapOf<String, FileEntity>()

    init {
        directoryMap["/"] = archiveFile
    }

    /**
     * Return the [FileEntity] that is the parent of the element denoted by [element]. If the directory does not
     * exist yet, it and its parents are created and inserted into database recursively.
     */
    fun getParentDirectory(element: String): FileEntity {
        val parentDirectoryPath = getParentPath(element)

        return directoryMap.getOrPut(parentDirectoryPath) {
            val directoryParent = getParentDirectory(parentDirectoryPath)
            val directoryParentPath = getParentPath(parentDirectoryPath)

            WorkspaceService.addDirectoryEntry(
                name = parentDirectoryPath.removePrefix(directoryParentPath),
                parent = directoryParent
            )
        }
    }

    /**
     * Parse the path of a archive element's parent element
     *
     * @param path the element path
     *
     * @return the parent path used in the archive builder maps
     */
    fun getParentPath(path: String): String {
        return (if (path.endsWith("/"))
            path.replace(lastDirectoryRegex, "")
        else
            path.removeRange(path.lastIndexOf('/').takeIf { it >= 0 }?.let { it + 1 }
                ?: 0, path.length))
            .ifEmpty { "/" }
    }
}