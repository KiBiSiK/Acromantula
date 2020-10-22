package net.cydhra.acromantula.commands.impl

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import kotlinx.serialization.Serializable
import net.cydhra.acromantula.commands.WorkspaceCommand
import net.cydhra.acromantula.commands.WorkspaceCommandArgs

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

class ImportCommandArgs(parser: ArgParser) : WorkspaceCommandArgs {
    val directory by parser
        .storing(
            "-d", "--directory",
            help = "where to place the file in the workspace file tree. leave empty for workspace root.",
            transform = { this.toInt() }
        )
        .default(null)

    val fileName by parser.positional("NAME", help = "name in the workspace file tree")

    val fileUrl by parser.positional("URL", help = "URL pointing to the file")

    override fun build() = ImportCommand(directory, fileName, fileUrl)
}