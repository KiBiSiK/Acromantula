package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ListCommandsCommandInterpreter
import org.apache.logging.log4j.LogManager

class ListCommandsCommandParser(parser: ArgParser) : WorkspaceCommandParser<List<String>> {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override fun build(): WorkspaceCommandInterpreter<List<String>> = ListCommandsCommandInterpreter()

    override fun report(result: Result<List<String>>) {
        logger.info("Available commands:\n" + result.getOrThrow().joinToString("\n"))
    }
}