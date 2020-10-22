package net.cydhra.acromantula.features.importer

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.DirectoryEntity
import org.apache.logging.log4j.LogManager
import java.io.PushbackInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal class ArchiveImporterStrategy : ImporterStrategy {

    companion object {
        private val logger = LogManager.getLogger()

        private const val LAST_DIRECTORY_PATTERN = "[^\\/]*\\/\$"
        private val lastDirectoryRegex = LAST_DIRECTORY_PATTERN.toRegex()
    }

    private val directoryMap = mutableMapOf<String, DirectoryEntity>()

    override fun handles(fileName: String, fileContent: PushbackInputStream): Boolean {
        // TODO maybe search magic bytes idk
        return fileName.endsWith(".zip") || fileName.endsWith(".jar")
    }

    override fun import(parent: DirectoryEntity?, fileName: String, fileContent: PushbackInputStream) {
        val archive = WorkspaceService.addArchiveEntry(fileName, parent)
        directoryMap["/"] = archive

        val zipInputStream = ZipInputStream(fileContent)

        var currentEntry: ZipEntry? = zipInputStream.nextEntry
        while (currentEntry != null) {
            logger.trace("importing ${currentEntry.name}")

            // do not test for `isDirectory` explicitly here, as java accepts zip files whose folders contain file
            // content. Just check whether content is available and treat it as a file, if there is.
            if (zipInputStream.available() > 0) {
                val parentDirectory = getParentDirectory(currentEntry.name)
                ImporterFeature.importFile(
                    parent = parentDirectory,
                    fileName = currentEntry.name.removePrefix(parentDirectory.name),
                    fileStream = zipInputStream
                )
            }

            // if it is not a file entry, do not add the directory explicitly, as zip standard does not require
            // directories to be even present. Just ignore empty directories, and add required ones on the fly.

            currentEntry = zipInputStream.nextEntry
        }
    }

    /**
     * Return the [DirectoryEntity] that is the parent of the element denoted by [element]. If the directory does not
     * exist yet, it and its parents are created recursively.
     */
    private fun getParentDirectory(element: String): DirectoryEntity {
        val parentDirectoryPath = getParentPath(element)

        return directoryMap.getOrPut(parentDirectoryPath) {
            val directoryParent = getParentDirectory(parentDirectoryPath)
            WorkspaceService.addDirectoryEntry(
                name = parentDirectoryPath.removePrefix(directoryParent.name),
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
    private fun getParentPath(path: String): String {
        return (if (path.endsWith("/"))
            path.replace(lastDirectoryRegex, "")
        else
            path.removeRange(path.lastIndexOf('/').takeIf { it >= 0 }?.let { it + 1 }
                ?: 0, path.length))
            .ifEmpty { "/" }
    }
}