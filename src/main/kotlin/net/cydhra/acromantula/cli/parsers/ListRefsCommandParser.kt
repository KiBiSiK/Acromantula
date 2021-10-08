package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ListRefsCommandInterpreter
import org.apache.logging.log4j.LogManager

class ListRefsCommandParser(parser: ArgParser) : WorkspaceCommandParser<List<Pair<Int, String>>> {

    companion object {
        private val logger = LogManager.getLogger()
    }

    val type by parser.positional("TYPE", help = "symbol type to search references to")

    val symbol by parser.positional("SYMBOL", help = "symbol identifier to get references to")

    override fun build(): WorkspaceCommandInterpreter<List<Pair<Int, String>>> =
        ListRefsCommandInterpreter(type, symbol)

    override fun report(result: Result<List<Pair<Int, String>>>) {
        result.onSuccess { references ->
            if (references.isEmpty()) {
                logger.info("no references to a symbol with that name")
            } else {
                val tree = references.joinToString("\n", transform = Pair<Int, String>::second)
                logger.info("References:\n$tree")
            }
        }
    }
}