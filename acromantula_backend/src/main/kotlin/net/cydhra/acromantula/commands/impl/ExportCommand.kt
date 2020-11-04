package net.cydhra.acromantula.commands.impl

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import net.cydhra.acromantula.commands.WorkspaceCommand
import net.cydhra.acromantula.commands.WorkspaceCommandArgs
import net.cydhra.acromantula.features.exporter.ExporterFeature
import net.cydhra.acromantula.workspace.WorkspaceService

/**
 * Command to import files into workspace.
 *
 * @param fileEntityId optional. the entity id of parent directory
 * @param filePath optional. the path of the directory in workspace
 * @param exporterName name of the exporter strategy to use
 * @param targetFileName path of target file
 */
@Suppress("DataClassPrivateConstructor")
@Serializable
data class ExportCommand private constructor(
    val fileEntityId: Int? = null,
    val filePath: String? = null,
    val exporterName: String,
    val targetFileName: String
) : WorkspaceCommand {

    /**
     * Command to import files into workspace.
     *
     * @param fileEntityId optional. the entity id of parent directory
     *  @param exporterName name of the exporter strategy to use
     * @param targetFileName name of target file
     */
    constructor(fileEntityId: Int? = null, exporterName: String, targetFileName: String) : this(
        fileEntityId, null, exporterName,
        targetFileName
    )

    /**
     * Command to import files into workspace.
     *
     * @param filePath optional. the path of the directory in workspace
     * @param exporterName name of the exporter strategy to use
     * @param targetFileName name of target file
     */
    constructor(filePath: String? = null, exporterName: String, targetFileName: String) : this(
        null, filePath,
        exporterName,
        targetFileName
    )

    override suspend fun evaluate() {
        val file = when {
            fileEntityId != null -> WorkspaceService.queryPath(fileEntityId)
            filePath != null -> WorkspaceService.queryPath(filePath)
            else -> throw IllegalStateException("either the entity id or the path of the file must be present")
        }

        withContext(Dispatchers.IO) {
            ExporterFeature.exportFile(file, exporterName, targetFileName)
        }
    }
}

class ExportCommandArgs(parser: ArgParser) : WorkspaceCommandArgs {
    val filePath by parser.positional("FILE", help = "file in workspace to export")

    val targetFileName by parser.positional("TARGET", help = "path of the target file")

    val exporter by parser.storing(
        "-e", "--exporter", help = "exporter to use. defaults to \"generic\""
    ).default("generic")

    override fun build() = ExportCommand(filePath, exporter, targetFileName)

}