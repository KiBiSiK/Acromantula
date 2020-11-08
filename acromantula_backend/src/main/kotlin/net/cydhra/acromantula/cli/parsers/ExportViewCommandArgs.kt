package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import net.cydhra.acromantula.cli.WorkspaceCommandArgs
import net.cydhra.acromantula.commands.interpreters.ExportViewCommandInterpreter

class ExportViewCommandArgs(parser: ArgParser) : WorkspaceCommandArgs {
    val filePath by parser.positional("FILE", help = "file in workspace to export")

    val targetFileName by parser.positional("TARGET", help = "path of the target file")

    val type by parser.positional("TYPE", help = "view type to export")

    val recursive by parser.flagging("-r", "--recursive", help = "whether to export a directory recursively")
        .default(false)

    val incompatible by parser.flagging(
        "-i", "--include-incompatible",
        help = "whether to also export incompatible files in the directory (if -r is set)"
    )
        .default(false)

    override fun build() = ExportViewCommandInterpreter(filePath, type, recursive, incompatible, targetFileName)
}