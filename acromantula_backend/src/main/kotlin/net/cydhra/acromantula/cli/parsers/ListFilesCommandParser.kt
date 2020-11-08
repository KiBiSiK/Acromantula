package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ListFilesCommandInterpreter

class ListFilesCommandParser(parser: ArgParser) : WorkspaceCommandParser {

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