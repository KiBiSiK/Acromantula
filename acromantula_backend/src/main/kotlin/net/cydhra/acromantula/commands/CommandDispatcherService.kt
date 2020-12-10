package net.cydhra.acromantula.commands

import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.pool.Task
import net.cydhra.acromantula.workspace.WorkspaceService
import org.apache.logging.log4j.LogManager

/**
 *
 */
object CommandDispatcherService : Service {

    override val name: String = "command dispatcher"

    override suspend fun initialize() {

    }

    private val logger = LogManager.getLogger()

    /**
     * Dispatch a command that originates from anywhere at the workspace. Dispatching it will schedule the command to
     * the worker pool and generate a status code, that can be used to request status information about the command.
     */
    fun dispatchCommand(commandInterpreter: WorkspaceCommandInterpreter): Task {
        logger.trace("launching command handler task for $commandInterpreter")
        return WorkspaceService.getWorkerPool().launchTask { commandInterpreter.evaluate() }
    }
}