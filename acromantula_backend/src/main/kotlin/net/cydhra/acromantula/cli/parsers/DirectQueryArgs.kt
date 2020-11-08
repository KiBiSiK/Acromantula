package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandArgs
import net.cydhra.acromantula.commands.interpreters.DirectQueryCommandInterpreter

class DirectQueryArgs(argParser: ArgParser) : WorkspaceCommandArgs {

    val query by argParser.positionalList("QUERY", help = "a raw SQL query")

    override fun build() = DirectQueryCommandInterpreter(this.query.joinToString(" "))
}