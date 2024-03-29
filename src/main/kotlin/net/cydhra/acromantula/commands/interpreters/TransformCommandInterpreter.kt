package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.transformer.TransformerFeature
import net.cydhra.acromantula.workspace.WorkspaceService

/**
 * Command to transform files in the workspace.
 *
 * @param fileEntityId optional. the entity id of parent directory
 * @param filePath optional. the path of the directory in workspace
 * @param transformer name of the transformer to use
 */
class TransformCommandInterpreter private constructor(
    val fileEntityId: Int? = null,
    val filePath: String? = null,
    val transformer: String
) : WorkspaceCommandInterpreter<Unit> {

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

        TransformerFeature.transformFiles(file, transformer)
    }
}

