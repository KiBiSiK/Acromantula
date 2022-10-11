package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.view.GenerateViewFeature
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.disassembly.FileRepresentation

/**
 * Command to delete file(s) from workspace.
 *
 * @param fileEntityId optional. the entity id of the file or directory to be deleted
 * @param filePath optional. the path of the file or directory to be deleted
 */
class DeleteCommandInterpreter private constructor(
    val fileEntityId: Int? = null,
    val filePath: String? = null,
) : WorkspaceCommandInterpreter<Unit> {

    /**
     * Command to import files into workspace.
     *
     * @param fileEntityId optional. the entity id of parent directory
     * @param type name of the generator strategy to use
     */
    constructor(fileEntityId: Int? = null) : this(fileEntityId, null)

    override val synchronous: Boolean = true

    /**
     * Command to import files into workspace.
     *
     * @param filePath optional. the path of the directory in workspace
     * @param type name of the generator strategy to use
     */
    constructor(filePath: String? = null) : this(null, filePath)

    override suspend fun evaluate() {
        val file = when {
            fileEntityId != null -> WorkspaceService.queryPath(fileEntityId)
            filePath != null -> WorkspaceService.queryPath(filePath)
            else -> throw IllegalStateException("either the entity id or the path of the file must be present")
        }

        WorkspaceService.deleteFile(file)
    }
}

