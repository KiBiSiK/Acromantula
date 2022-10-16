package net.cydhra.acromantula.features.importer

import kotlinx.coroutines.CompletableJob
import net.cydhra.acromantula.features.archives.ArchiveFeature
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.PushbackInputStream

/**
 * A general fallback strategy used to import files that are not handled by any other strategy
 */
internal class GenericFileImporterStrategy : ImporterStrategy {

    override suspend fun handles(fileName: String, fileContent: PushbackInputStream): Boolean {
        return true
    }

    override suspend fun import(
        supervisor: CompletableJob,
        parent: FileEntity?,
        fileName: String,
        fileContent: PushbackInputStream
    ): Pair<FileEntity, ByteArray> {
        val content = fileContent.readBytes()
        val file = ArchiveFeature.createFile(fileName, parent, content)
        return Pair(file, content)
    }
}