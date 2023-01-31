package net.cydhra.acromantula.commands.interpreters

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.importer.ImporterFeature
import net.cydhra.acromantula.features.importer.ImporterJob
import net.cydhra.acromantula.workspace.WorkspaceService
import java.net.MalformedURLException
import java.net.URL

/**
 * Command to import files into workspace.
 *
 * @param directory optional. the entity id of parent directory
 * @param directoryPath optional. the path of the directory in workspace
 * @param fileUrl URL pointing to the file
 * @param partialResultChannel a channel through which the importer can report partial results to the user
 */
class ImportCommandInterpreter(
    private val directory: Int? = null,
    private val directoryPath: String? = null,
    private val fileUrl: String,
    private val partialResultChannel: Channel<ImporterJob.ImportProgressEvent>? = null
) : WorkspaceCommandInterpreter<Unit> {

    /**
     * Command to import files into workspace.
     *
     * @param directory optional. the entity id of parent directory
     * @param fileUrl URL pointing to the file
     * @param partialResultChannel a channel through which the importer can report partial results to the user
     */
    constructor(
        directory: Int? = null, fileUrl: String, partialResultChannel: Channel<ImporterJob.ImportProgressEvent>? = null
    ) : this(directory, null, fileUrl, partialResultChannel)

    /**
     * Command to import files into workspace.
     *
     * @param directoryPath optional. the path of the directory in workspace
     * @param fileUrl URL pointing to the file
     * @param partialResultChannel a channel through which the importer can report partial results to the user
     */
    constructor(
        directoryPath: String? = null,
        fileUrl: String,
        partialResultChannel: Channel<ImporterJob.ImportProgressEvent>? = null
    ) : this(null, directoryPath, fileUrl, partialResultChannel)

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

        ImporterFeature.startImportJob(parentDirectoryEntity, sourceFile, partialResultChannel)
    }
}

