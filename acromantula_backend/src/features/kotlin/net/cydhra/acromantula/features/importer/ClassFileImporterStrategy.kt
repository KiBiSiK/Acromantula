package net.cydhra.acromantula.features.importer

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.DirectoryEntity
import java.io.PushbackInputStream

internal class ClassFileImporterStrategy : ImporterStrategy {

    override fun handles(fileName: String, fileContent: PushbackInputStream): Boolean {
        var readSize: Int = 0
        try {
            val buffer = ByteArray(4)
            readSize = fileContent.read(buffer, 0, 4)

            return readSize == 4 && byteArrayOf(
                0xca.toByte(),
                0xfe.toByte(),
                0xba.toByte(),
                0xbe.toByte(),
            ).contentEquals(buffer)
        } finally {
            fileContent.unread(readSize)
        }
    }

    override fun import(parent: DirectoryEntity?, fileName: String, fileContent: PushbackInputStream) {
        WorkspaceService.addClassEntry(fileName, parent, fileContent.readBytes())
    }
}