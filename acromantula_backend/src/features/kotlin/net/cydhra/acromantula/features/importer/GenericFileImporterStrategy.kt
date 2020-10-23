package net.cydhra.acromantula.features.importer

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.DirectoryEntity
import java.io.PushbackInputStream

/**
 * A general fallback strategy used to import files that are not handled by any other strategy
 */
internal class GenericFileImporterStrategy : ImporterStrategy {

    override fun handles(fileName: String, fileContent: PushbackInputStream): Boolean {
        return true
    }

    override fun import(parent: DirectoryEntity?, fileName: String, fileContent: PushbackInputStream) {
        WorkspaceService.addFileEntry(fileName, parent, fileContent.readBytes())
    }
}