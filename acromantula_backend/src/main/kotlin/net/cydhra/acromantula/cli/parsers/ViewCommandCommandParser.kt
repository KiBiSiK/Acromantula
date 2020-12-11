package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ViewCommandInterpreter

class ViewCommandCommandParser(parser: ArgParser) : WorkspaceCommandParser {
    val filePath by parser.positional("FILE", help = "file in workspace to export")

    val type by parser.positional("TYPE", help = "how to generate the view")

    override fun build(): WorkspaceCommandInterpreter<*> = ViewCommandInterpreter(filePath, type)

}