package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * Command to list all files in a directory.
 * // TODO: this should work recursively. Design a SQL query to achieve that, but for now just do it for a directory
 * @param directoryPath directory path. If null, either `directoryId` must be set, or the root directory is referred
 * @param directoryId directory id. If null, either `directory` must be set, or the root directory is referred
 */
class ListFilesCommandInterpreter(
    val directoryPath: String? = null,
    val directoryId: Int? = null
) : WorkspaceCommandInterpreter<List<FileEntity>> {
    override val synchronous: Boolean = true

    /**
     * List files in the directory denoted by the given path
     */
    constructor(directoryPath: String? = null) : this(directoryPath, null)

    /**
     * List files in the directory denoted by the given id
     */
    constructor(directoryId: Int? = null) : this(null, directoryId)

    override suspend fun evaluate(): List<FileEntity> {
        return when {
            directoryId != null -> listOf(WorkspaceService.queryPath(directoryId))
            directoryPath != null -> listOf(WorkspaceService.queryPath(directoryPath))
            else -> WorkspaceService.listFiles()
        }
    }
}

