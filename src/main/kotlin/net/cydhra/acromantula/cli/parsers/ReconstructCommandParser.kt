package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ReconstructCommandInterpreter
import net.cydhra.acromantula.workspace.WorkspaceService
import java.net.URL

class ReconstructCommandParser(parser: ArgParser) : WorkspaceCommandParser<Unit> {
    val filePath by parser.positional(
        "PATH",
        help = "path of a file in workspace to replace by a reconstruction of a view"
    )

    val representationSource by parser.positional(
        "URL",
        help = "URL pointing to the representation file to reconstruct"
    )

    val viewType by parser.positional(
        "TYPE",
        help = "representation type to reconstruct into a file"
    )

    override fun build(): WorkspaceCommandInterpreter<Unit> {
        val fileId = WorkspaceService.queryPath(filePath).resource
        val buffer = URL(representationSource).openStream().readBytes()
        return ReconstructCommandInterpreter(fileId, viewType, buffer)
    }

    override fun report(result: Result<Unit>) {

    }

}