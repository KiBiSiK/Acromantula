package net.cydhra.acromantula.features.import

import net.cydhra.acromantula.data.filesystem.DirectoryEntity
import java.io.PushbackInputStream

internal class ClassFileImporterStrategy : ImporterStrategy {

    override fun handles(fileName: String, fileContent: PushbackInputStream): Boolean {
        var readSize: Int = 0
        try {
            val buffer = ByteArray(4)
            readSize = fileContent.read(buffer, 0, 4)

            return readSize == 4 && byteArrayOf(
                0xca.toUByte().toByte(),
                0xfe.toUByte().toByte(),
                0xba.toUByte().toByte(),
                0xbe.toUByte().toByte(),
            ).contentEquals(buffer)
        } finally {
            fileContent.unread(readSize)
        }
    }

    override fun import(parent: DirectoryEntity, fileName: String, fileContent: PushbackInputStream) {
        // TODO parse to ClassNode, hand to worker to parse, insert file into tree
    }
}