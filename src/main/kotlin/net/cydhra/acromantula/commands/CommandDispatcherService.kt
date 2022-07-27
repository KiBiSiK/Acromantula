package net.cydhra.acromantula.commands

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import net.cydhra.acromantula.pool.SupervisedTask
import net.cydhra.acromantula.pool.TaskScheduler
import org.apache.logging.log4j.LogManager

/**
 *
 */
object CommandDispatcherService {

    fun initialize() {

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

        return if (commandInterpreter.synchronous) {
            CoroutineScope(Dispatchers.Unconfined).async {
                try {
                    Result.success(commandInterpreter.evaluate())
                } catch (t: Throwable) {
                    Result.failure(t)
                }
            }
        } else {
            TaskScheduler.schedule(SupervisedTask(taskName) {
                commandInterpreter.evaluate()
            })
        }
    }
}