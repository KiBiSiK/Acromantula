package net.cydhra.acromantula.features.importer

import kotlinx.coroutines.CompletableJob
import net.cydhra.acromantula.features.util.FileTreeBuilder
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayInputStream
import java.io.PushbackInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal class ArchiveImporterStrategy : ImporterStrategy {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override suspend fun handles(fileName: String, fileContent: PushbackInputStream): Boolean {
        // TODO maybe search magic bytes idk
        return fileName.endsWith(".zip") || fileName.endsWith(".jar")
    }

    override suspend fun import(
        supervisor: CompletableJob,
        parent: FileEntity?,
        fileName: String,
        fileContent: PushbackInputStream
    ):
            Pair<FileEntity, ByteArray?> {
        val archive = WorkspaceService.addArchiveEntry(fileName, parent)
        val treeBuilder = FileTreeBuilder(archive)

        val zipInputStream = ZipInputStream(fileContent)

        var currentEntry: ZipEntry? = zipInputStream.nextEntry
        while (currentEntry != null) {
            logger.trace("found zip entry: \"${currentEntry.name}\"")

            // do not test for `isDirectory` explicitly here, as java accepts zip files whose folders contain file
            // content. Just check whether content is available and treat it as a file, if there is.
            // Unfortunately reading the data is the only way to see if data is available, because `available()`
            // violates its contract in `ZipInputStream`. So we are a little bit inefficient in our use of streams here.
            val blob = zipInputStream.readBytes()
            if (blob.isNotEmpty()) {
                val parentDirectory = treeBuilder.getParentDirectory(currentEntry.name)
                val parentDirectoryName = treeBuilder.getParentPath(currentEntry.name)
                ImporterFeature.importFile(
                    supervisor = supervisor,
                    parent = parentDirectory,
                    fileName = currentEntry.name.removePrefix(parentDirectoryName),
                    fileStream = PushbackInputStream(ByteArrayInputStream(blob))
                )
            }

            // if it is not a file entry, do not add the directory explicitly, as zip standard does not require
            // directories to be even present. Just ignore empty directories, and add required ones on the fly.

            currentEntry = zipInputStream.nextEntry
        }

        return Pair(archive, null)
    }
}