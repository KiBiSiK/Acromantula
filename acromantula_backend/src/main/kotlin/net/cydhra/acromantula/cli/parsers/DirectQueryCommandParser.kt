package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.DirectQueryCommandInterpreter

class DirectQueryCommandParser(argParser: ArgParser) : WorkspaceCommandParser<Unit> {

    val query by argParser.positionalList("QUERY", help = "a raw SQL query")

    override fun build(): WorkspaceCommandInterpreter<Unit> =
        DirectQueryCommandInterpreter(this.query.joinToString(" "))

    override fun report(result: Result<Unit>) {

    }
}