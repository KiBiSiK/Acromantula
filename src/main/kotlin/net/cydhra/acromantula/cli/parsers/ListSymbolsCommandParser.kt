package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ListSymbolsCommandInterpreter
import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import org.apache.logging.log4j.LogManager

class ListSymbolsCommandParser(parser: ArgParser) : WorkspaceCommandParser<List<AcromantulaSymbol>> {

    companion object {
        private val logger = LogManager.getLogger()
    }

    val file by parser.positional("FILE", help = "from which file to list symbols")

    override fun build(): WorkspaceCommandInterpreter<List<AcromantulaSymbol>> =
        ListSymbolsCommandInterpreter(file)

    override fun report(result: Result<List<AcromantulaSymbol>>) {
        result.onSuccess { list ->
            logger.info("Symbols in file: \n" + list.joinToString("\n", transform = AcromantulaSymbol::displayString))
        }
    }
}