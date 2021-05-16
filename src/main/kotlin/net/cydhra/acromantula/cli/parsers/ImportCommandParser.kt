package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ImportCommandInterpreter
import org.apache.logging.log4j.LogManager

class ImportCommandParser(parser: ArgParser) : WorkspaceCommandParser<Unit> {
    companion object {
        private val logger = LogManager.getLogger()
    }

    val directory by parser
        .storing(
            "-d", "--directory",
            help = "path where to place the file in the workspace file tree. leave empty for workspace root.",
        )
        .default(null)

    val fileUrl by parser.positional("URL", help = "URL pointing to the file")

    override fun build(): WorkspaceCommandInterpreter<Unit> = ImportCommandInterpreter(directory, fileUrl)

    override fun report(result: Result<Unit>) {
        logger.info("import of \"$fileUrl\" ${if (result.isSuccess) "successful" else "failed"}")
    }
}