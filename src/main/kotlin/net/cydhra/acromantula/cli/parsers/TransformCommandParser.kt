package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.TransformCommandInterpreter
import org.apache.logging.log4j.LogManager.getLogger as logger

class TransformCommandParser(parser: ArgParser) : WorkspaceCommandParser<Unit> {
    val filePath by parser.positional("FILE", help = "file in workspace to apply transformation")

    val transformer by parser.positional("TRANSFORMER", help = "transformer to use")

    override fun build(): WorkspaceCommandInterpreter<Unit> = TransformCommandInterpreter(filePath, transformer)

    override fun report(result: Result<Unit>) {
        result.onSuccess {
            logger().info("transformation successful")
        }
    }
}