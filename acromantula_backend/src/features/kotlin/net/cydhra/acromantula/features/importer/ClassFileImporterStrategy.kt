package net.cydhra.acromantula.features.importer

import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.PushbackInputStream

internal class ClassFileImporterStrategy : ImporterStrategy {

    override fun handles(fileName: String, fileContent: PushbackInputStream): Boolean {
        var readSize: Int = 0
        val buffer = ByteArray(4)

        try {
            readSize = fileContent.read(buffer, 0, 4)

            return readSize == 4 && byteArrayOf(
                0xca.toByte(),
                0xfe.toByte(),
                0xba.toByte(),
                0xbe.toByte(),
            ).contentEquals(buffer)
        } finally {
            fileContent.unread(buffer.copyOfRange(0, readSize))
        }
    }

    override fun import(parent: FileEntity?, fileName: String, fileContent: PushbackInputStream) {
        WorkspaceService.addClassEntry(fileName, parent, fileContent.readBytes())
    }
}