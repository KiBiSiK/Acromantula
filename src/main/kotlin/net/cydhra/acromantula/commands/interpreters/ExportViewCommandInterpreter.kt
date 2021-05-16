package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.exporter.ExporterFeature
import net.cydhra.acromantula.features.exporter.GENERIC_EXPORTER_STRATEGY
import net.cydhra.acromantula.features.view.GenerateViewFeature
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Command to export one ore more file views from workspace. This can be used to convert archives or singular files
 * and then export them.
 *
 * @param fileEntityId optional. the entity id of parent directory
 * @param filePath optional. the path of the directory in workspace
 * @param viewType which view type to export
 * @param recursive whether to recursively export a whole directory
 * @param includeIncompatible whether to include files in the directory that are incompatible with the exporter
 * @param targetFileName path of target file
 */
class ExportViewCommandInterpreter private constructor(
    val fileEntityId: Int? = null,
    val filePath: String? = null,
    val viewType: String,
    val recursive: Boolean,
    val includeIncompatible: Boolean,
    val targetFileName: String
) : WorkspaceCommandInterpreter<Unit> {

    companion object {
        private val logger = LogManager.getLogger()
    }

    /**
     * Command to export one ore more file views from workspace. This can be used to convert archives or singular files
     * and then export them.
     *
     * @param fileEntityId optional. the entity id of parent directory
     * @param viewType which view type to export
     * @param recursive whether to recursively export a whole directory
     * @param includeIncompatible whether to include files in the directory that are incompatible with the exporter
     * @param targetFileName path of target file
     */
    constructor(
        fileEntityId: Int? = null,
        viewType: String,
        recursive: Boolean,
        includeIncompatible: Boolean,
        targetFileName: String
    ) : this(fileEntityId, null, viewType, recursive, includeIncompatible, targetFileName)

    /**
     * Command to export one ore more file views from workspace. This can be used to convert archives or singular files
     * and then export them.
     *
     * @param filePath optional. the path of the directory in workspace
     * @param viewType which view type to export
     * @param recursive whether to recursively export a whole directory
     * @param includeIncompatible whether to include files in the directory that are incompatible with the exporter
     * @param targetFileName path of target file
     */
    constructor(
        filePath: String? = null,
        viewType: String,
        recursive: Boolean,
        includeIncompatible: Boolean,
        targetFileName: String
    ) : this(null, filePath, viewType, recursive, includeIncompatible, targetFileName)

    override suspend fun evaluate() {
        val file = when {
            fileEntityId != null -> WorkspaceService.queryPath(fileEntityId)
            filePath != null -> WorkspaceService.queryPath(filePath)
            else -> throw IllegalStateException("either the entity id or the path of the file must be present")
        }

        if (recursive) {
            val outputStream = ZipOutputStream(FileOutputStream(targetFileName))
            val files = WorkspaceService.getDirectoryContent(file)
            appendRepresentations(files, "", outputStream)
            outputStream.close()
        } else {
            val representation = GenerateViewFeature.generateView(file, viewType)
            if (representation == null) {
                logger.error("cannot create view of \"${file.name}\"")
            } else {
                val outputFileStream = FileOutputStream(targetFileName)
                outputFileStream.use { stream ->
                    GenerateViewFeature.exportView(representation, stream)
                }
                logger.info("exported view \"$targetFileName\" of \"${file.name}\"")
            }
        }
    }

    /**
     * Append the representations of all given files to an output stream. If any given file is a directory, its
     * contents will be added recursively. If [includeIncompatible] is true, incompatible files will be exported
     * directly into the output stream.
     *
     * @param files a representation of all those files, and any children (of directories) will be exported
     * @param prefix path prefix to the files. Use empty string for calling the method
     * @param outputStream the zip file where to export everything
     */
    private fun appendRepresentations(files: List<FileEntity>, prefix: String, outputStream: ZipOutputStream) {
        for (subFile in files) {
            val zipEntryName = if (prefix.isNotEmpty()) prefix + File.pathSeparatorChar + subFile.name else subFile.name

            if (subFile.isDirectory) {
                outputStream.putNextEntry(ZipEntry(zipEntryName))
                outputStream.closeEntry()

                appendRepresentations(WorkspaceService.getDirectoryContent(subFile), zipEntryName, outputStream)
            } else {
                val representation =
                    GenerateViewFeature.generateView(subFile, viewType)

                if (representation == null) {
                    logger.error("cannot create view of \"${subFile.name}\"")

                    if (includeIncompatible) {
                        outputStream.putNextEntry(ZipEntry(subFile.name))
                        ExporterFeature.exportFile(subFile, GENERIC_EXPORTER_STRATEGY, outputStream)
                        outputStream.closeEntry()
                        logger.info("exported content of \"${subFile.name}\" instead")
                    }
                } else {
                    var fileName = zipEntryName
                    val extension = GenerateViewFeature.getFileExtension(viewType)
                    if (extension != null) {
                        fileName += ".$extension"
                    }

                    outputStream.putNextEntry(ZipEntry(fileName))
                    GenerateViewFeature.exportView(representation, outputStream)
                    logger.info("exported view \"$targetFileName\" of \"${subFile.name}\"")
                }
            }
        }
    }
}

