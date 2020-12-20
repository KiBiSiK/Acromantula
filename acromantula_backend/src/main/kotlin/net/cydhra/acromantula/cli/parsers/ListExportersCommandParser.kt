package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ListExportersCommandInterpreter
import org.apache.logging.log4j.LogManager

class ListExportersCommandParser(parser: ArgParser) : WorkspaceCommandParser<List<String>> {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override fun build(): WorkspaceCommandInterpreter<List<String>> = ListExportersCommandInterpreter()

    override fun report(result: Result<List<String>>) {
        result.onSuccess {
            logger.info("available exporters: ${it.joinToString()}")
        }
    }
}