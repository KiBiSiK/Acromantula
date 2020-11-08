package net.cydhra.acromantula.commands.impl

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import kotlinx.serialization.Serializable
import net.cydhra.acromantula.commands.WorkspaceCommandArgs
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.workspace.WorkspaceService

/**
 * Command to list all files in a directory.
 * // TODO: this should work recursively. Design a SQL query to achieve that, but for now just do it for a directory
 * @param directoryPath directory path. If null, either `directoryId` must be set, or the root directory is referred
 * @param directoryId directory id. If null, either `directory` must be set, or the root directory is referred
 */
@Suppress("DataClassPrivateConstructor")
@Serializable
data class ListFilesCommandInterpreter private constructor(
    val directoryPath: String? = null,
    val directoryId: Int? = null
) : WorkspaceCommandInterpreter {

    /**
     * List files in the directory denoted by the given path
     */
    constructor(directoryPath: String? = null) : this(directoryPath, null)

    /**
     * List files in the directory denoted by the given id
     */
    constructor(directoryId: Int? = null) : this(null, directoryId)

    override suspend fun evaluate() {
        val directory = when {
            directoryId != null -> WorkspaceService.queryPath(directoryId)
            directoryPath != null -> WorkspaceService.queryPath(directoryPath)
            else -> null
        }
        WorkspaceService.listFiles(root = directory).forEach {
            println(it.name)
        }
    }
}

class ListFilesParser(parser: ArgParser) : WorkspaceCommandArgs {

    val directoryPath by parser.storing("-d", "-p", "--path", help = "directory path").default(null)

    val directoryId by parser.storing("-i", "--identifier",
        help = "directory identifier",
        transform = { toInt() }).default(null)

    override fun build(): WorkspaceCommandInterpreter =
        if (directoryPath != null)
            ListFilesCommandInterpreter(directoryPath)
        else
            ListFilesCommandInterpreter(directoryId)
}