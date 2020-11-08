package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandArgs
import net.cydhra.acromantula.commands.interpreters.ViewCommandInterpreter

class ViewCommandArgs(parser: ArgParser) : WorkspaceCommandArgs {
    val filePath by parser.positional("FILE", help = "file in workspace to export")

    val type by parser.positional("TYPE", help = "how to generate the view")

    override fun build() = ViewCommandInterpreter(filePath, type)

}