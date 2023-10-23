package net.cydhra.acromantula.features.view.prefabs

import net.cydhra.acromantula.features.view.DocumentGenerator
import net.cydhra.acromantula.features.view.ViewGeneratorStrategy
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.disassembly.FileViewEntity
import net.cydhra.acromantula.workspace.disassembly.MediaType
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.zip.InflaterInputStream
import kotlin.experimental.and

const val LENGTH = "length"
const val TYPE = "type"
const val ANCILLARY = "ancillary"
const val STANDARDIZED = "standardized"
const val SAFE_TO_COPY = "safe to copy"
const val CRC = "crc"
const val CONTENT = "content"

val CHUNK_TYPE_START = convertChunkTypeToInt("IHDR")
val CHUNK_TYPE_PHYSICAL_PIXEL_DIM = convertChunkTypeToInt("pHYs")
val CHUNK_TYPE_TEXT = convertChunkTypeToInt("tEXt")
val CHUNK_TYPE_COMPRESSED_TEXT = convertChunkTypeToInt("zTXt")
val CHUNK_TYPE_INTERNATIONAL_TEXT = convertChunkTypeToInt("iTXt")

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

                    val chunk = when (type) {
                        CHUNK_TYPE_START -> Chunk.StartChunk(fileContent)
                        CHUNK_TYPE_TEXT -> Chunk.TextChunk(fileContent, length)
                        CHUNK_TYPE_COMPRESSED_TEXT -> Chunk.CompressedTextChunk(fileContent, length)
                        CHUNK_TYPE_INTERNATIONAL_TEXT -> Chunk.InternationalTextChunk(fileContent, length)
                        CHUNK_TYPE_PHYSICAL_PIXEL_DIM -> Chunk.PhysicalPixelDimensionChunk(fileContent)
                        else -> Chunk.IgnoredChunk(fileContent, length)
                    }

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
                            if (chunk !is Chunk.IgnoredChunk) {
                                collapsible(chunk.chunkDescription, chunk.printChunk())
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

private sealed class Chunk(val chunkDescription: String) {

    class StartChunk(buffer: ByteBuffer) : Chunk("metadata") {
        val width: Int
        val height: Int
        val bitDepth: Int
        val colorType: Int
        val compressionMethod: Int
        val filterMethod: Int
        val interlaceMethod: Int

        init {
            width = buffer.getInt()
            height = buffer.getInt()
            bitDepth = buffer.get().toInt()
            colorType = buffer.get().toInt()
            compressionMethod = buffer.get().toInt()
            filterMethod = buffer.get().toInt()
            interlaceMethod = buffer.get().toInt()
        }

        override fun printChunk(): DocumentGenerator.TopLevelGenerator.() -> Unit = {
            text("Width: $width")
            br()
            text("Height: $height")
            br()
            text("Bit Depth: $bitDepth")
            br()
            text("Color Type: ")
            if (colorType and 1 > 0) { text("palette used, ") } else { text("no palette, ") }
            if (colorType and 2 > 0) { text("color used, ") } else { text("no color, ") }
            if (colorType and 4 > 0) { text("alpha used") } else { text("no alpha") }
            br()

            text("Compression Method: ")
            if (compressionMethod == 0) { text("deflate") } else { text("illegal value: $compressionMethod") }
            br()

            text("Filter Method: ")
            if (compressionMethod == 0) { text("adaptive filter") } else { text("illegal value: $compressionMethod") }
            br()

            text("Interlace Method: ")
            when (interlaceMethod) {
                0 -> text("no interlace")
                1 -> text("Adam7 interlace")
                else -> text("illegal value: $interlaceMethod")
            }
        }
    }

    class IgnoredChunk(buffer: ByteBuffer, length: Int) : Chunk("") {
        init {
            buffer.position(buffer.position() + length)
        }
    }

    class TextChunk(buffer: ByteBuffer, length: Int) : Chunk("text chunk") {
        val text: String
        val keyword: String

        init {
            val textArray = ByteArray(length)
            buffer.get(textArray, 0, length)
            val keyValuePair = String(textArray, Charsets.ISO_8859_1).split(Char(0))
            keyword = keyValuePair[0]
            text = keyValuePair[1]
        }

        override fun printChunk(): DocumentGenerator.TopLevelGenerator.() -> Unit = {
            text("$keyword: $text")
        }
    }

    class CompressedTextChunk(buffer: ByteBuffer, length: Int) : Chunk("compressed text chunk") {
        val keyword: String
        val text: String
        var warnings: String = ""

        init {
            val keywordBuffer = ByteBuffer.allocate(80)
            var byte = buffer.get()
            while (byte != 0.toByte()) {
                keywordBuffer.put(byte)
                byte = buffer.get()
            }
            keyword = String(keywordBuffer.array(), Charsets.ISO_8859_1)

            val compressionMethod = buffer.get().toInt()
            if (compressionMethod != 0) { warnings += "illegal compression method: $compressionMethod\n" }

            val valueBuffer = ByteArray(length - keywordBuffer.position() - 1)

            buffer.get(valueBuffer, 0, length - keyword.length - 1)

            text = try {
                val inflater = InflaterInputStream(ByteArrayInputStream(valueBuffer))
                String(inflater.use { it.readBytes() }, Charsets.ISO_8859_1)
            } catch (e: Exception) {
                warnings += "inflate decompression failed: ${e.message}"
                LogManager.getLogger().error("inflate decompression failed", e)
                "compressed data corrupted"
            }
        }

        override fun printChunk(): DocumentGenerator.TopLevelGenerator.() -> Unit = {
            text("$keyword: $text")
            if (warnings.isNotBlank()) {
                br()
                text("Chunk Error:")
                br()
                text(warnings)
            }
        }
    }

    class InternationalTextChunk(buffer: ByteBuffer, length: Int) : Chunk("international text chunk") {
        val keyword: String
        val languageTag: String
        val translatedKeyword: String
        val text: String
        var warnings: String = ""
        val isCompressed: Boolean

        init {
            keyword = readZeroTerminatedString(buffer, Charsets.ISO_8859_1)
            isCompressed = buffer.get().toInt() != 0

            val compressionMethod = buffer.get().toInt()
            if (isCompressed) {
                if (compressionMethod != 0) { warnings += "illegal compression method: $compressionMethod\n" }
            }

            this.languageTag = readZeroTerminatedString(buffer, Charsets.ISO_8859_1)
            this.translatedKeyword = readZeroTerminatedString(buffer, Charsets.UTF_8)

            val valueBuffer = ByteArray(length - (keyword.length + 1) - 2 - (languageTag.length + 1) - (translatedKeyword.length + 1))
            buffer.get(valueBuffer, 0, valueBuffer.size)

            text = if (isCompressed) {
                try {
                    val inflater = InflaterInputStream(ByteArrayInputStream(valueBuffer))
                    String(inflater.use { it.readBytes() }, Charsets.UTF_8)
                } catch (e: Exception) {
                    warnings += "inflate decompression failed: ${e.message}"
                    LogManager.getLogger().error("inflate decompression failed", e)
                    "compressed data corrupted"
                }
            } else {
                String(valueBuffer, Charsets.UTF_8)
            }
        }


        override fun printChunk(): DocumentGenerator.TopLevelGenerator.() -> Unit = {
            if (isCompressed) {
                text("(deflate compressed): ")
            }

            text(keyword )

            if (languageTag.isNotEmpty() || translatedKeyword.isNotBlank()) {
                text(" (")
                if (languageTag.isNotBlank()) {
                    text(languageTag)
                    if (translatedKeyword.isNotBlank()) {
                        text(": ")
                    }
                }
                if (translatedKeyword.isNotBlank()) {
                    text(translatedKeyword)
                }
                text(" ): ")
            } else {
                text(": ")
            }

            text(text)

            if (warnings.isNotBlank()) {
                br()
                text("Chunk Error:")
                br()
                text(warnings)
            }
        }
    }

    class PhysicalPixelDimensionChunk(buffer: ByteBuffer) : Chunk("physical pixel dimensions") {
        val xAxis: Int
        val yAxis: Int
        val unit: Unit

        init {
            xAxis = buffer.getInt()
            yAxis = buffer.getInt()
            unit = Unit.entries[buffer.get().toInt()]
        }

        override fun printChunk(): DocumentGenerator.TopLevelGenerator.() -> kotlin.Unit = {
            text(
                "X Axis: $xAxis texel per ${
                    if (unit == Unit.METER) {
                        "meter"
                    } else {
                        "pixel"
                    }
                }"
            )
            br()
            text(
                "Y Axis: $yAxis texel per ${
                    if (unit == Unit.METER) {
                        "meter"
                    } else {
                        "pixel"
                    }
                }"
            )
        }

        enum class Unit {
            UNSPECIFIED, METER
        }

    }

    /**
     * Print the chunk into the document table
     */
    open fun printChunk(): DocumentGenerator.TopLevelGenerator.() -> Unit = {}

    protected fun readZeroTerminatedString(inputBuffer: ByteBuffer, charset: Charset): String {
        var stringBuffer = ByteBuffer.allocate(80)
        var byte = inputBuffer.get()
        var tagLength = 0
        var resultString = ""
        while (byte != 0.toByte()) {
            stringBuffer.put(byte)
            tagLength += 1
            if (tagLength == 80) {
                resultString += String(stringBuffer.array(), charset)
                stringBuffer = ByteBuffer.allocate(80)
                tagLength = 0
            }

            byte = inputBuffer.get()
        }

        if (tagLength > 0) {
            resultString += String(stringBuffer.array().sliceArray(0 until tagLength))
        }

        return resultString
    }
}