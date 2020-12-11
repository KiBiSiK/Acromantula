package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.QuitCommandInterpreter

class QuitCommandParser(parser: ArgParser) : WorkspaceCommandParser {

    override fun build(): WorkspaceCommandInterpreter<*> = QuitCommandInterpreter()
}