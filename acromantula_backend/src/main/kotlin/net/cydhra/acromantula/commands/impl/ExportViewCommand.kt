package net.cydhra.acromantula.commands.impl

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import net.cydhra.acromantula.commands.WorkspaceCommand
import net.cydhra.acromantula.commands.WorkspaceCommandArgs
import net.cydhra.acromantula.features.view.GenerateViewFeature
import net.cydhra.acromantula.workspace.WorkspaceService
import org.apache.logging.log4j.LogManager
import java.io.FileOutputStream
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
@Suppress("DataClassPrivateConstructor")
@Serializable
data class ExportViewCommand private constructor(
    val fileEntityId: Int? = null,
    val filePath: String? = null,
    val viewType: String,
    val recursive: Boolean,
    val includeIncompatible: Boolean,
    val targetFileName: String
) : WorkspaceCommand {

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

        withContext(Dispatchers.IO) {
            if (recursive) {
                ZipOutputStream(FileOutputStream(targetFileName))
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
    }
}

class ExportViewCommandArgs(parser: ArgParser) : WorkspaceCommandArgs {
    val filePath by parser.positional("FILE", help = "file in workspace to export")

    val targetFileName by parser.positional("TARGET", help = "path of the target file")

    val type by parser.positional("TYPE", help = "view type to export")

    val recursive by parser.flagging("-r", "--recursive", help = "whether to export a directory recursively")
        .default(false)

    val incompatible by parser.flagging(
        "-i", "--include-incompatible",
        help = "whether to also export incompatible files in the directory (if -r is set)"
    )
        .default(false)

    override fun build() = ExportViewCommand(filePath, type, recursive, incompatible, targetFileName)
}