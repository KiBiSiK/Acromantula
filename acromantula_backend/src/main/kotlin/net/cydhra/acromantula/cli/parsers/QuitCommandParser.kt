package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.QuitCommandInterpreter
import java.util.*

class QuitCommandParser(parser: ArgParser) : WorkspaceCommandParser<Unit> {

    override fun build(): WorkspaceCommandInterpreter<Unit> = QuitCommandInterpreter()

    override fun report(result: Optional<out Result<Unit>>) {
        println("shutting down server...")
    }
}