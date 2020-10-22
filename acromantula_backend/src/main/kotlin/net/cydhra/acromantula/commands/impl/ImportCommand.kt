package net.cydhra.acromantula.commands.impl

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import net.cydhra.acromantula.commands.WorkspaceCommand
import net.cydhra.acromantula.commands.WorkspaceCommandArgs
import net.cydhra.acromantula.features.importer.ImporterFeature
import net.cydhra.acromantula.workspace.WorkspaceService
import java.net.MalformedURLException
import java.net.URL

/**
 * Command to import files into workspace.
 *
 * @param directory optional. the entity id of parent directory
 * @param directoryPath optional. the path of the directory in workspace
 * @param fileName name of the file in the workspace file tree
 * @param fileUrl URL pointing to the file
 */
@Suppress("DataClassPrivateConstructor")
@Serializable
data class ImportCommand private constructor(
    val directory: Int? = null,
    val directoryPath: String? = null,
    val fileName: String,
    val fileUrl: String
) : WorkspaceCommand {

    /**
     * Command to import files into workspace.
     *
     * @param directory optional. the entity id of parent directory
     * @param fileName name of the file in the workspace file tree
     * @param fileUrl URL pointing to the file
     */
    constructor(directory: Int? = null, fileName: String, fileUrl: String) : this(directory, null, fileName, fileUrl)

    /**
     * Command to import files into workspace.
     *
     * @param directoryPath optional. the path of the directory in workspace
     * @param fileName name of the file in the workspace file tree
     * @param fileUrl URL pointing to the file
     */
    constructor(directoryPath: String? = null, fileName: String, fileUrl: String) : this(
        null,
        directoryPath,
        fileName,
        fileUrl
    )

    override suspend fun evaluate() {
        val sourceFile = try {
            // TODO how to parse URLs without blocking? Why does this block anyway?
            URL(fileUrl)
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("cannot import $fileName", e)
        }

        val parentDirectoryEntity = when {
            directory != null -> WorkspaceService.queryDirectory(directory)
            directoryPath != null -> WorkspaceService.queryDirectory(directoryPath)
            else -> null
        }

        withContext(Dispatchers.IO) {
            ImporterFeature.importFile(parentDirectoryEntity, fileName, sourceFile)
        }
    }
}

class ImportCommandArgs(parser: ArgParser) : WorkspaceCommandArgs {
    val directory by parser
        .storing(
            "-d", "--directory",
            help = "path where to place the file in the workspace file tree. leave empty for workspace root.",
        )
        .default(null)

    val fileName by parser.positional("NAME", help = "name in the workspace file tree")

    val fileUrl by parser.positional("URL", help = "URL pointing to the file")

    override fun build() = ImportCommand(directory, fileName, fileUrl)

}