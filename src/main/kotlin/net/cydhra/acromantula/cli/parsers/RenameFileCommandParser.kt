package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.RenameFileCommandInterpreter
import org.apache.logging.log4j.LogManager

class RenameFileCommandParser(parser: ArgParser) : WorkspaceCommandParser<Unit> {
    val filePath by parser.positional("FILE", help = "file or directory in workspace.")

    val newName by parser.positional("NAME", help = "new file name")

    override fun build(): WorkspaceCommandInterpreter<Unit> = RenameFileCommandInterpreter(filePath, newName)

    override fun report(result: Result<Unit>) {
        result.onSuccess {
            LogManager.getLogger().info("file renamed to \"$newName\"")
        }
    }
}