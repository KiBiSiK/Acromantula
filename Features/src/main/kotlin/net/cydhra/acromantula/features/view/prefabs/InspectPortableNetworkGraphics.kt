package net.cydhra.acromantula.features.view.prefabs

import net.cydhra.acromantula.features.view.DocumentGenerator
import net.cydhra.acromantula.features.view.ViewGeneratorStrategy
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.disassembly.FileViewEntity
import net.cydhra.acromantula.workspace.disassembly.MediaType
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.ByteBuffer
import kotlin.experimental.and

const val LENGTH = "length"
const val TYPE = "type"
const val ANCILLARY = "ancillary"
const val STANDARDIZED = "standardized"
const val SAFE_TO_COPY = "safe to copy"
const val CRC = "crc"

object InspectPortableNetworkGraphics : ViewGeneratorStrategy {
    override val viewType: String = "ixpng"
    override val fileType: MediaType = MediaType.HTML

    override fun handles(fileEntity: FileEntity): Boolean {
        val buffer = ByteArray(8)
        val readSize = WorkspaceService.getFileContent(fileEntity).use {
            it.read(buffer, 0, 8)
        }

        // PNG and EOF magic bytes
        return readSize == 8 && byteArrayOf(
            0x89.toByte(),
            0x50.toByte(),
            0x4E.toByte(),
            0x47.toByte(),
            0x0D.toByte(),
            0x0A.toByte(),
            0x1A.toByte(),
            0x0A.toByte()
        ).contentEquals(buffer)
    }

    override fun generateView(fileEntity: FileEntity): FileViewEntity {
        val document = DocumentGenerator()
        val fileContent = ByteBuffer.wrap(WorkspaceService.getFileContent(fileEntity).use { it.readBytes() })

        // skip magic bytes
        fileContent.getLong()

        document.table(LENGTH, TYPE, ANCILLARY, STANDARDIZED, SAFE_TO_COPY, CRC) {
            while (fileContent.hasRemaining()) {
                val length = fileContent.getInt()

                val typeByte1 = fileContent.get()
                val typeByte2 = fileContent.get()
                val typeByte3 = fileContent.get()
                val typeByte4 = fileContent.get()

                fileContent.position(fileContent.position() + length)

                // read length bytes
                val crc = fileContent.getInt()

                row(mapOf(
                    LENGTH to length.toString(),
                    TYPE to typeByte1.toChar().toString() + typeByte2.toChar().toString() + typeByte3.toChar()
                        .toString() + typeByte4.toChar().toString(),
                    ANCILLARY to if (typeByte1 and 32 == 0.toByte()) { "critical" } else { "ancillary" },
                    STANDARDIZED to if (typeByte2 and 32 == 0.toByte()) { "yes" } else { "no" },
                    SAFE_TO_COPY to if (typeByte4 and 32 == 0.toByte()) { "no" } else { "yes" },
                    CRC to crc.toString()
                ))
            }
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        document.writeHtml(OutputStreamWriter(byteArrayOutputStream))

        return WorkspaceService.addFileRepresentation(
            fileEntity, this.viewType, this.fileType, byteArrayOutputStream.toByteArray()
        )
    }
}