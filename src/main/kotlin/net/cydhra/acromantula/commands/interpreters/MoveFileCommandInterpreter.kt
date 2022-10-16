package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.archives.ArchiveFeature
import net.cydhra.acromantula.workspace.WorkspaceService

/**
 * Command to delete file(s) from workspace.
 *
 * @param sourceFileId optional. the entity id of the file
 * @param sourceFilePath optional. the path of the source file. Either this or sourceFileId must be present
 * @param targetFolderId optional. the id of the target folder
 * @param targetFolderPath optional. the path of the target folder. If empty string, workspace root is assumed
 */
class MoveFileCommandInterpreter(
    val sourceFileId: Int? = null,
    val sourceFilePath: String? = null,
    val targetFolderId: Int? = null,
    val targetFolderPath: String? = null,
) : WorkspaceCommandInterpreter<Unit> {

    override val synchronous: Boolean = true

    override suspend fun evaluate() {
        val file = when {
            sourceFileId != null -> WorkspaceService.queryPath(sourceFileId)
            sourceFilePath != null -> WorkspaceService.queryPath(sourceFilePath)
            else -> throw IllegalArgumentException("either sourceFileId or sourceFilePath must be present")
        }

        val targetDirectory = when {
            targetFolderId != null -> WorkspaceService.queryPath(targetFolderId)
            !targetFolderPath.isNullOrEmpty() -> WorkspaceService.queryPath(targetFolderPath)
            else -> null
        }

        ArchiveFeature.moveFile(file, targetDirectory)
    }
}

