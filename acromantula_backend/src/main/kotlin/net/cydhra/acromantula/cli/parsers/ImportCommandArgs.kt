package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import net.cydhra.acromantula.cli.WorkspaceCommandArgs
import net.cydhra.acromantula.commands.interpreters.ImportCommandInterpreter

class ImportCommandArgs(parser: ArgParser) : WorkspaceCommandArgs {
    val directory by parser
        .storing(
            "-d", "--directory",
            help = "path where to place the file in the workspace file tree. leave empty for workspace root.",
        )
        .default(null)

    val fileUrl by parser.positional("URL", help = "URL pointing to the file")

    override fun build() = ImportCommandInterpreter(directory, fileUrl)

}