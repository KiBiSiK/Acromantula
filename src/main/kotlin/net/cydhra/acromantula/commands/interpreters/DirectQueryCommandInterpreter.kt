package net.cydhra.acromantula.commands.interpreters

import kotlinx.coroutines.CompletableJob
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.workspace.WorkspaceService
import org.apache.logging.log4j.LogManager

/**
 * A command to directly query the workspace database. This is meant as a debug command to look at the current
 * database layout. This should not be available in a production build
 */
class DirectQueryCommandInterpreter(val query: String) : WorkspaceCommandInterpreter<Unit> {
    companion object {
        private val logger = LogManager.getLogger()
    }

    override suspend fun evaluate(supervisor: CompletableJob) {
        val resultSet = WorkspaceService.directQuery(this.query)
        resultSet.forEach(::println)
        logger.debug("finished SQL query")
    }
}

