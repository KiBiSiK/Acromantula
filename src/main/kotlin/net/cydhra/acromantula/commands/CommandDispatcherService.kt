package net.cydhra.acromantula.commands

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import net.cydhra.acromantula.bus.Service
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
     * the worker pool and attach it to the given supervisor job, so spawning child jobs can be awaited by the caller.
     */
    fun <T> dispatchCommand(
        supervisor: CompletableJob,
        commandInterpreter: WorkspaceCommandInterpreter<T>
    ): Deferred<Result<T>> {
        logger.trace("launching command handler task for $commandInterpreter")
        return WorkspaceService.getWorkerPool().submit(supervisor) { commandInterpreter.evaluate() }
    }

    /**
     * Dispatch a command and await its completion, along with the completion of all child jobs that originated from
     * this command.
     */
    suspend fun <T> dispatchCommandSupervised(commandInterpreter: WorkspaceCommandInterpreter<T>): Result<T> {
        val supervisor = SupervisorJob()
        val result = dispatchCommand(supervisor, commandInterpreter).await()
        supervisor.complete()
        return result
    }
}