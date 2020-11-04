package net.cydhra.acromantula.commands.impl

import com.xenomachina.argparser.ArgParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import net.cydhra.acromantula.commands.WorkspaceCommand
import net.cydhra.acromantula.commands.WorkspaceCommandArgs
import net.cydhra.acromantula.features.view.GenerateViewFeature
import net.cydhra.acromantula.workspace.WorkspaceService
import org.apache.logging.log4j.LogManager

/**
 * Command to import files into workspace.
 *
 * @param fileEntityId optional. the entity id of parent directory
 * @param filePath optional. the path of the directory in workspace
 * @param type name of the generator strategy to use
 */
@Suppress("DataClassPrivateConstructor")
@Serializable
data class ViewCommand private constructor(
    val fileEntityId: Int? = null,
    val filePath: String? = null,
    val type: String
) : WorkspaceCommand {

    /**
     * Command to import files into workspace.
     *
     * @param fileEntityId optional. the entity id of parent directory
     * @param type name of the generator strategy to use
     */
    constructor(fileEntityId: Int? = null, type: String) : this(fileEntityId, null, type)

    /**
     * Command to import files into workspace.
     *
     * @param filePath optional. the path of the directory in workspace
     * @param type name of the generator strategy to use
     */
    constructor(filePath: String? = null, type: String) : this(null, filePath, type)

    override suspend fun evaluate() {
        val file = when {
            fileEntityId != null -> WorkspaceService.queryPath(fileEntityId)
            filePath != null -> WorkspaceService.queryPath(filePath)
            else -> throw IllegalStateException("either the entity id or the path of the file must be present")
        }

        withContext(Dispatchers.IO) {
            val viewResource = GenerateViewFeature.generateView(file, type)

            if (viewResource == null)
                LogManager.getLogger().info("cannot create view of type \"$type\" for \"${file.name}\"")
            else {
                LogManager.getLogger().info("created view in new resource")
            }
        }
    }
}

class ViewCommandArgs(parser: ArgParser) : WorkspaceCommandArgs {
    val filePath by parser.positional("FILE", help = "file in workspace to export")

    val type by parser.positional("TYPE", help = "how to generate the view")

    override fun build() = ViewCommand(filePath, type)

}