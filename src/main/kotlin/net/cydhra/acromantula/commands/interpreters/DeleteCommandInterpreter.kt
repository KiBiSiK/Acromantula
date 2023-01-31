package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.archives.ArchiveFeature
import net.cydhra.acromantula.workspace.WorkspaceService

/**
 * Command to delete file(s) from workspace.
 *
 * @param fileEntityId optional. the entity id of the file or directory to be deleted
 * @param filePath optional. the path of the file or directory to be deleted
 */
class DeleteCommandInterpreter private constructor(
    val fileEntityId: Int?,
    val filePath: String?,
) : WorkspaceCommandInterpreter<Unit> {

    override val synchronous: Boolean = true

    /**
     * Command to delete file(s) from workspace.
     *
     * @param fileEntityId optional. the entity id of the file or directory to be deleted
     */
    constructor(fileEntityId: Int? = null) : this(fileEntityId, null)

    /**
     * Command to delete file(s) from workspace.
     *
     * @param filePath optional. the path of the file or directory to be deleted
     */
    constructor(filePath: String? = null) : this(null, filePath)

    override suspend fun evaluate() {
        val file = when {
            fileEntityId != null -> WorkspaceService.queryPath(fileEntityId)
            filePath != null -> WorkspaceService.queryPath(filePath)
            else -> throw IllegalStateException("either the entity id or the path of the file must be present")
        }

        ArchiveFeature.deleteFile(file)
    }
}

