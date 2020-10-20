package net.cydhra.acromantula.workspace.worker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import java.util.concurrent.Executors

/**
 * Provides different scopes for asynchronous dispatching of work.
 */
class WorkerPool {

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
        return workerCoroutineScope.async(block = worker)
    }
}