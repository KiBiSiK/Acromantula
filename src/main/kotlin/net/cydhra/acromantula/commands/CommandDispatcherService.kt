package net.cydhra.acromantula.commands

import kotlinx.coroutines.Deferred
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.pool.SupervisedTask
import net.cydhra.acromantula.pool.TaskScheduler
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
     *
     * @param taskName name of the dispatched task for displaying
     */
    fun <T> dispatchCommand(
        taskName: String,
        commandInterpreter: WorkspaceCommandInterpreter<T>
    ): Deferred<Result<T>> {
        logger.trace("launching command handler task for $commandInterpreter")
        return TaskScheduler.schedule(SupervisedTask(taskName) {
            commandInterpreter.evaluate()
        })
    }
}