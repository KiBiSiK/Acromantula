package net.cydhra.acromantula.pool

import kotlinx.coroutines.Deferred
import org.apache.logging.log4j.LogManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A scheduler for [SupervisedTasks][SupervisedTask]
 */
object TaskScheduler {

    /**
     * An internal thread pool that uses all available system threads.
     */
    private lateinit var threadPool: ExecutorService

    /**
     * List of currently running tasks
     */
    private val runningTasks = mutableListOf<SupervisedTask<*>>()

    fun initialize() {
        LogManager.getLogger()
            .debug(
                "create fixed thread pool for worker tasks " +
                        "with ${Runtime.getRuntime().availableProcessors()} threads"
            )
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    }

    /**
     * Schedule a [SupervisedTask] for execution and return its [Deferred] result
     */
    fun <T> scheduleAsync(supervisedTask: SupervisedTask<T>): Deferred<Result<T>> {
        synchronized(runningTasks) {
            runningTasks.add(supervisedTask)
        }
        supervisedTask.onCompletion { removeTask(supervisedTask) }
        return supervisedTask.startAsync(this.threadPool)
    }

    /**
     * @return a list of all names of currently running [SupervisedTasks][SupervisedTask]
     */
    fun runningTaskNames(): List<String> {
        return synchronized(runningTasks) {
            runningTasks.map(SupervisedTask<*>::name).toList()
        }
    }

    /**
     * Remove a task from the [runningTasks] list in a thread-safe way.
     */
    private fun removeTask(supervisedTask: SupervisedTask<*>) {
        synchronized(runningTasks) {
            runningTasks.remove(supervisedTask)
        }
    }

    fun onShutdown() {
        LogManager.getLogger().debug("shutdown fixed thread pool for worker threads...")
        threadPool.shutdown()
    }
}