package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.archives.ArchiveFeature
import net.cydhra.acromantula.workspace.WorkspaceService

/**
 * Command to delete file(s) from workspace.
 *
 * @param fileEntityId optional. the entity id of the parent directory
 * @param filePath optional. the path of the parent directory. If fileEntityId is -1 and path is an empty string,
 * workspace root is assumed
 * @param fileName name of the new file
 * @param createDirectory whether to create a directory instead of a file
 */
class CreateFileCommandInterpreter private constructor(
    val fileEntityId: Int? = null,
    val filePath: String? = null,
    val fileName: String,
    val createDirectory: Boolean,
) : WorkspaceCommandInterpreter<Unit> {

    /**
     * Command to create a new file in workspace.
     *
     * @param fileEntityId optional. the entity id of the parent directory
     * @param fileName name of the new file
     * @param createDirectory whether to create a directory instead of a file
     */
    constructor(fileEntityId: Int? = null, fileName: String, createDirectory: Boolean) :
            this(fileEntityId, null, fileName, createDirectory)

    override val synchronous: Boolean = true

    /**
     * Command to create a new file in workspace.
     *
     * @param filePath The path of the parent directory. If this is an empty string, workspace root is assumed
     * @param fileName name of the new file
     * @param createDirectory whether to create a directory instead of a file
     */
    constructor(filePath: String? = null, fileName: String, createDirectory: Boolean)
            : this(null, filePath, fileName, createDirectory)

    override suspend fun evaluate() {
        val file = when {
            fileEntityId != null -> WorkspaceService.queryPath(fileEntityId)
            !filePath.isNullOrBlank() -> WorkspaceService.queryPath(filePath)
            else -> null
        }

        if (createDirectory) {
            ArchiveFeature.addDirectory(fileName, file)
        } else {
            ArchiveFeature.createFile(fileName, file, ByteArray(0))
        }
    }
}

