package net.cydhra.acromantula.commands.impl

import kotlinx.serialization.Serializable
import net.cydhra.acromantula.commands.WorkspaceCommand

/**
 * Command to import files into workspace.
 *
 * @param directory optional. the entity id of parent directory
 * @param fileName name of the file in the workspace file tree
 * @param fileUrl URL pointing to the file
 */
@Serializable
data class ImportCommand(
    val directory: Int? = null,
    val fileName: String,
    val fileUrl: String
) : WorkspaceCommand {

    override suspend fun evaluate() {

    }
}