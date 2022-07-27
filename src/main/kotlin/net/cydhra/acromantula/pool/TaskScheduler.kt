package net.cydhra.acromantula.pool

import kotlinx.coroutines.Deferred

/**
 * A scheduler for [SupervisedTasks][SupervisedTask]
 */
object TaskScheduler {

    /**
     * List of currently running tasks
     */
    private val runningTasks = mutableListOf<SupervisedTask<*>>()

    /**
     * Schedule a [SupervisedTask] for execution and return its [Deferred] result
     */
    fun <T> schedule(supervisedTask: SupervisedTask<T>): Deferred<Result<T>> {
        synchronized(runningTasks) {
            runningTasks.add(supervisedTask)
        }
        supervisedTask.onCompletion { removeTask(supervisedTask) }
        return supervisedTask.start()
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
}