package net.cydhra.acromantula.commands.interpreters

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.importer.ImporterFeature
import net.cydhra.acromantula.workspace.WorkspaceService
import java.net.MalformedURLException
import java.net.URL

/**
 * Command to import files into workspace.
 *
 * @param directory optional. the entity id of parent directory
 * @param directoryPath optional. the path of the directory in workspace
 * @param fileUrl URL pointing to the file
 */
class ImportCommandInterpreter(
    val directory: Int? = null, val directoryPath: String? = null, val fileUrl: String
) : WorkspaceCommandInterpreter<Unit> {

    /**
     * Command to import files into workspace.
     *
     * @param directory optional. the entity id of parent directory
     * @param fileUrl URL pointing to the file
     */
    constructor(directory: Int? = null, fileUrl: String) : this(directory, null, fileUrl)

    /**
     * Command to import files into workspace.
     *
     * @param directoryPath optional. the path of the directory in workspace
     * @param fileUrl URL pointing to the file
     */
    constructor(directoryPath: String? = null, fileUrl: String) : this(null, directoryPath, fileUrl)

    override suspend fun evaluate() {
        val sourceFile = withContext(Dispatchers.IO) {
            try {
                URL(fileUrl)
            } catch (e: MalformedURLException) {
                throw IllegalArgumentException("cannot import \"$fileUrl\"", e)
            }
        }

        val parentDirectoryEntity = when {
            directory != null -> WorkspaceService.queryPath(directory)
            directoryPath != null -> WorkspaceService.queryPath(directoryPath)
            else -> null
        }

        ImporterFeature.importFile(parentDirectoryEntity, sourceFile)
    }
}

