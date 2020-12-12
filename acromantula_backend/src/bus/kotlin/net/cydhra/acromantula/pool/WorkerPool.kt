package net.cydhra.acromantula.pool

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.*
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.pool.event.TaskFinishedEvent
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Provides different scopes for asynchronous dispatching of work.
 */
class WorkerPool {

    private val logger = LogManager.getLogger()

    /**
     * A cached thread pool for low-utilisation/long-running or asymmetrical work load.
     */
    private val cachedThreadPool = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool())

    /**
     * A work-stealing pool that is used for heavy-duty worker threads
     */
    private val workerPool = Executors.newWorkStealingPool()

    /**
     * A work-stealing threadpool for heavy-duty jobs that can easily be paralleled and require high amounts of green
     * threads. Use this for actual work on data. The pool is limited to the number of logical cores available.
     */
    private val workerCoroutineScope = CoroutineScope(workerPool.asCoroutineDispatcher())

    /**
     * A list of tasks registered and not yet collected. They might have finished their work.
     */
    private val registeredTasks = mutableListOf<Task<*>>()

    /**
     * Unique id counter.
     */
    private var id: Int = 0

    /**
     * Submit a heavy duty task and return a deferred promise. The task is scheduled in a work stealing threadpool.
     */
    fun <T> submit(worker: suspend CoroutineScope.() -> T): Deferred<T> {
        logger.trace("launching worker in bounded thread pool...")
        return workerCoroutineScope.async(block = worker)
    }

    /**
     * Launch a potentially long running task in an unbounded pool of threads. This should not perform heavy duty
     * work, because that might starve the actual worker threads.
     *
     * @param initialStatus initial [Task.status]
     * @param autoReap automatically reap the job upon completion. Use this for fire-and-forget jobs
     * @param runnable the task method
     */
    fun <V> launchTask(
        initialStatus: String = "job scheduled",
        autoReap: Boolean = false,
        runnable: () -> V
    ): Task<V> {
        logger.trace("launching task in unbounded thread pool...")
        val future = cachedThreadPool.submit(Callable(runnable))

        @Suppress("UNCHECKED_CAST")
        val task = Task(id++, future, initialStatus)
        this.registeredTasks.add(task)

        // task finished event listener
        Futures.addCallback(future, object : FutureCallback<V> {
            override fun onSuccess(result: V?) {
                EventBroker.fireEvent(TaskFinishedEvent(task.id))
                task.finished = true
                task.result = result?.let { Result.success(it) }?.let { Optional.of(it) } ?: Optional.empty()
            }

            override fun onFailure(t: Throwable) {
                EventBroker.fireEvent(TaskFinishedEvent(task.id))
                task.finished = true
                task.result = Optional.of(Result.failure(t))
            }
        }, this.cachedThreadPool)

        if (autoReap) {
            // auto reap listener
            Futures.addCallback(future, object : FutureCallback<V> {
                override fun onSuccess(result: V?) {
                    logger.debug("reaped task ${task.id} (exit status: \"${task.status}\")")
                    this@WorkerPool.registeredTasks.remove(task)
                }

                override fun onFailure(t: Throwable) {
                    logger.error("auto-reap task failed:", t)
                }
            }, this.cachedThreadPool)
        }

        return task
    }

    fun shutdown() {
        logger.info("awaiting cached thread pool termination (timeout 60 seconds)...")
        this.cachedThreadPool.shutdown()
        if (!this.cachedThreadPool.awaitTermination(60L, TimeUnit.SECONDS)) {
            logger.info("timeout elapsed. attempting shutdown by force.")
            this.cachedThreadPool.shutdownNow()
        }
        logger.info("cached thread pool terminated.")

        logger.info("awaiting worker thread pool termination (timeout 60 seconds)...")
        this.workerPool.shutdown()
        if (!this.workerPool.awaitTermination(60L, TimeUnit.SECONDS)) {
            logger.info("timeout elapsed. forcing shutdown...")
            this.workerPool.shutdownNow()
        }
        logger.info("worker thread pool terminated.")
    }

    /**
     * Remove the task from the task list if it is finished. Returns the task, or null, if the task is still running.
     */
    fun reap(taskId: Int): Task<*>? {
        val task = this.registeredTasks.find { it.id == taskId }
            ?: throw IllegalArgumentException("this task does not exist")

        if (task.finished) {
            this.registeredTasks.remove(task)
            return task
        }

        return null
    }

    /**
     * Get a list of all tasks currently registered in the pool. It includes running tasks and finished tasks that
     * were not reaped yet.
     */
    fun listTasks(): List<Task<*>> {
        return this.registeredTasks
    }
}