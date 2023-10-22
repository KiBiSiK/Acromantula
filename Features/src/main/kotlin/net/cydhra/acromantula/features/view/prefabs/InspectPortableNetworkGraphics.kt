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
const val CONTENT = "content"

val CHUNK_TYPE_PHYSICAL_PIXEL_DIM = convertChunkTypeToInt("pHYs")
val CHUNK_TYPE_TEXT = convertChunkTypeToInt("tEXt")
val CHUNK_TYPE_COMPRESSED_TEXT = convertChunkTypeToInt("zTXt")
val CHUNK_TYPE_INTERNATIONAL_TEXT = convertChunkTypeToInt("iTXt")

const val UNIT_UNSPECIFIED = 0
const val UNIT_METER = 1

fun convertChunkTypeToInt(type: String): Int {
    check(type.length == 4) { "chunk type must be exactly 4 bytes long" }

    var result = 0
    for (c in type.chars()) {
        result = result shl 8
        result = result or c
    }

    return result
}

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

        document.append {
            table(LENGTH, TYPE, ANCILLARY, STANDARDIZED, SAFE_TO_COPY, CRC, CONTENT) {
                while (fileContent.hasRemaining()) {
                    val length = fileContent.getInt()

                    val typeByte1 = fileContent.get()
                    val typeByte2 = fileContent.get()
                    val typeByte3 = fileContent.get()
                    val typeByte4 = fileContent.get()
                    val type = (typeByte1.toInt() shl 24) or (typeByte2.toInt() shl 16) or (typeByte3.toInt() shl 8) or
                            typeByte4.toInt()

                    val oldPosition = fileContent.position()

                    fileContent.position(oldPosition + length)

                    // read length bytes
                    val crc = fileContent.getInt()

                    row {
                        cell {
                            text(length.toString())
                        }
                        cell {
                            text(
                                typeByte1.toChar().toString() + typeByte2.toChar().toString() + typeByte3.toChar()
                                    .toString() + typeByte4.toChar().toString()
                            )
                        }
                        cell {
                            text(
                                if (typeByte1 and 32 == 0.toByte()) {
                                    "critical"
                                } else {
                                    "ancillary"
                                }
                            )
                        }
                        cell {
                            text(
                                if (typeByte2 and 32 == 0.toByte()) {
                                    "yes"
                                } else {
                                    "no"
                                }
                            )
                        }
                        cell {
                            text(
                                if (typeByte4 and 32 == 0.toByte()) {
                                    "no"
                                } else {
                                    "yes"
                                }
                            )
                        }
                        cell {
                            text(crc.toUInt().toString())
                        }
                        cell {
                            when (type) {
                                CHUNK_TYPE_PHYSICAL_PIXEL_DIM -> collapsible("physical pixel dimensions") {
                                    fileContent.position(oldPosition)
                                    val xAxis = fileContent.getInt()
                                    val yAxis = fileContent.getInt()
                                    val unit = fileContent.get().toInt()

                                    fileContent.getInt() // skip crc

                                    text(
                                        "X Axis: $xAxis texels per ${
                                            if (unit == UNIT_METER) {
                                                "meter"
                                            } else {
                                                "pixel"
                                            }
                                        }"
                                    )
                                    br()
                                    text(
                                        "Y Axis: $yAxis texels per ${
                                            if (unit == UNIT_METER) {
                                                "meter"
                                            } else {
                                                "pixel"
                                            }
                                        }"
                                    )
                                }

                                CHUNK_TYPE_TEXT -> collapsible("text chunk") {
                                    val textArray = ByteArray(length)
                                    fileContent.position(oldPosition)
                                    fileContent.get(textArray, 0, length)
                                    fileContent.getInt() // skip crc
                                    text(String(textArray, Charsets.ISO_8859_1))
                                }

                                CHUNK_TYPE_COMPRESSED_TEXT -> collapsible("compressed text chunk") {
                                    text("not yet decompressed")
                                }

                                CHUNK_TYPE_INTERNATIONAL_TEXT -> collapsible("international text chunk") {
                                    val textArray = ByteArray(length)
                                    fileContent.position(oldPosition)
                                    fileContent.get(textArray, 0, length)
                                    fileContent.getInt() // skip crc
                                    text(String(textArray, Charsets.UTF_8))
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        document.writeHtml(OutputStreamWriter(byteArrayOutputStream))

        return WorkspaceService.addFileRepresentation(
            fileEntity, this.viewType, this.fileType, byteArrayOutputStream.toByteArray()
        )
    }
}