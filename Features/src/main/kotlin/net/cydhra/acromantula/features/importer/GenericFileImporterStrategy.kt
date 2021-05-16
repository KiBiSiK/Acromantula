package net.cydhra.acromantula.features.importer

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.PushbackInputStream

/**
 * A general fallback strategy used to import files that are not handled by any other strategy
 */
internal class GenericFileImporterStrategy : ImporterStrategy {

    override suspend fun handles(fileName: String, fileContent: PushbackInputStream): Boolean {
        return true
    }

    override suspend fun import(parent: FileEntity?, fileName: String, fileContent: PushbackInputStream) {
        WorkspaceService.addFileEntry(fileName, parent, fileContent.readBytes())
    }
}