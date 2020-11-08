package net.cydhra.acromantula.commands.impl

import com.xenomachina.argparser.ArgParser
import kotlinx.serialization.Serializable
import net.cydhra.acromantula.commands.WorkspaceCommandArgs
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.workspace.WorkspaceService
import org.apache.logging.log4j.LogManager

/**
 * A command to directly query the workspace database. This is meant as a debug command to look at the current
 * database layout. This should not be available in a production build
 */
@Serializable
data class DirectQueryCommandInterpreter(val query: String) : WorkspaceCommandInterpreter {
    companion object {
        private val logger = LogManager.getLogger()
    }

    override suspend fun evaluate() {
        val resultSet = WorkspaceService.directQuery(this.query)
        resultSet.forEach(::println)
        logger.debug("finished SQL query")
    }
}

class DirectQueryArgs(argParser: ArgParser) : WorkspaceCommandArgs {

    val query by argParser.positionalList("QUERY", help = "a raw SQL query")

    override fun build() = DirectQueryCommandInterpreter(this.query.joinToString(" "))
}