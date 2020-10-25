package net.cydhra.acromantula.workspace.worker

import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
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
    private val cachedThreadPool = Executors.newCachedThreadPool()

    /**
     * A thread pool with an unlimited number of threads. Use this for threads that do not perform heavy-duty tasks
     * or if it is unclear how many threads of its type are scheduled. Do not this to perform actual work on data
     */
    private val unboundedCoroutineScope = CoroutineScope(cachedThreadPool.asCoroutineDispatcher())

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
     * Submit a heavy duty task and return a deferred promise. The task is scheduled in a work stealing threadpool.
     */
    fun <T> submit(worker: suspend CoroutineScope.() -> T): Deferred<T> {
        logger.trace("launching worker in bounded thread pool...")
        return workerCoroutineScope.async(block = worker)
    }

    /**
     * Launch a potentially long running task in an unbounded pool of threads. This should not perform heavy duty
     * work, because that might starve the actual worker threads.
     */
    fun launchTask(task: suspend CoroutineScope.() -> Unit): Job {
        logger.trace("launching task in unbounded thread pool...")
        return unboundedCoroutineScope.launch {
            try {
                this.task()
            } catch (t: Throwable) {
                logger.error("job failed", t)
            }
        }
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
}