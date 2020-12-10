package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
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
class ViewCommandInterpreter private constructor(
    val fileEntityId: Int? = null,
    val filePath: String? = null,
    val type: String
) : WorkspaceCommandInterpreter {

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

    override fun evaluate() {
        val file = when {
            fileEntityId != null -> WorkspaceService.queryPath(fileEntityId)
            filePath != null -> WorkspaceService.queryPath(filePath)
            else -> throw IllegalStateException("either the entity id or the path of the file must be present")
        }

        val viewResource = GenerateViewFeature.generateView(file, type)

        if (viewResource == null)
            LogManager.getLogger().info("cannot create view of type \"$type\" for \"${file.name}\"")
        else {
            LogManager.getLogger().info("view available as resource")
        }
    }
}

