package net.cydhra.acromantula.features.importer

import net.cydhra.acromantula.workspace.filesystem.DirectoryEntity
import java.io.PushbackInputStream

internal class GenericFileImporterStrategy : ImporterStrategy {

    override fun handles(fileName: String, fileContent: PushbackInputStream): Boolean {
        return true
    }

    override fun import(parent: DirectoryEntity?, fileName: String, fileContent: PushbackInputStream) {
        // Todo copy file content into workspace
    }
}