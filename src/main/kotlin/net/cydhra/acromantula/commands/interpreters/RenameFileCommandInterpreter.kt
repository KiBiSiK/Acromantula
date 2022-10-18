package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.archives.ArchiveFeature
import net.cydhra.acromantula.workspace.WorkspaceService

/**
 * Command to rename a file in workspace.
 *
 * @param fileEntityId optional. the entity id of the file or directory to be deleted
 * @param filePath optional. the path of the file or directory to be deleted
 * @param newName new file name
 */
class RenameFileCommandInterpreter private constructor(
    private val fileEntityId: Int? = null,
    private val filePath: String? = null,
    private val newName: String
) : WorkspaceCommandInterpreter<Unit> {

    /**
     * Command to rename a file in workspace.
     *
     * @param fileEntityId the entity id of the file or directory to be renamed
     * @param newName new file name
     */
    constructor(fileEntityId: Int? = null, newName: String) : this(fileEntityId, null, newName)

    override val synchronous: Boolean = true

    /**
     * Command to rename a file in workspace.
     *
     * @param filePath the path of the file or directory to be renamed
     * @param newName new file name
     */
    constructor(filePath: String? = null, newName: String) : this(null, filePath, newName)

    override suspend fun evaluate() {
        val file = when {
            fileEntityId != null -> WorkspaceService.queryPath(fileEntityId)
            filePath != null -> WorkspaceService.queryPath(filePath)
            else -> throw IllegalStateException("either the entity id or the path of the file must be present")
        }

        ArchiveFeature.renameFile(file, newName)
    }
}

