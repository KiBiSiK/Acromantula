package net.cydhra.acromantula.pool

import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import org.apache.logging.log4j.LogManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Provides different scopes for asynchronous dispatching of work.
 * TODO: change the API so tasks are submitted as packets instead of lambdas, so a list of currently running tasks
 *  (independent of running coroutines) can be requested
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
    fun <T> submit(worker: suspend CoroutineScope.() -> T): Deferred<Result<T>> {
        logger.trace("launching worker in bounded thread pool...")
        return workerCoroutineScope.async(block = {
            try {
                worker.invoke(this).let { Result.success(it) }
            } catch (t: Throwable) {
                logger.error("worker task crashed: ", t)
                Result.failure(t)
            }
        })
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
     * Get a list of all tasks currently registered in the pool. It includes running tasks and finished tasks that
     * were not reaped yet.
     */
    fun listTasks(): List<Task<*>> {
        return this.registeredTasks
    }
}