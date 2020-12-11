package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ExportCommandInterpreter

class ExportCommandCommandParser(parser: ArgParser) : WorkspaceCommandParser {
    val filePath by parser.positional("FILE", help = "file in workspace to export")

    val targetFileName by parser.positional("TARGET", help = "path of the target file")

    val exporter by parser.storing(
        "-e", "--exporter", help = "exporter to use. defaults to \"generic\""
    ).default("generic")

    override fun build(): WorkspaceCommandInterpreter<*> = ExportCommandInterpreter(filePath, exporter, targetFileName)

}