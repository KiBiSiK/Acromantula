package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.RenameCommandInterpreter
import org.apache.logging.log4j.LogManager.getLogger as logger

class RenameCommandParser(parser: ArgParser) : WorkspaceCommandParser<Unit> {
    val symbolType by parser.positional("TYPE", help = "type of the symbol that gets renamed")
    val symbolName by parser.positional("NAME", help = "current name of the symbol that gets renamed")
    val newName by parser.positional("NEW", help = "new name for the symbol")

    override fun build(): WorkspaceCommandInterpreter<Unit> = RenameCommandInterpreter(symbolType, symbolName, newName)

    override fun report(result: Result<Unit>) {
        result.onSuccess {
            logger().info("renaming successful")
        }
    }
}