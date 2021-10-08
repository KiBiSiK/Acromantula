package net.cydhra.acromantula.pool

import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.apache.logging.log4j.LogManager.getLogger as logger

/**
 * A coherent task that is invoked by some user interaction and consists of (multiple) complex workloads that use
 * coroutines opaquely to increase throughput. A task is submitted by the command scheduler that handles user
 * interaction and a list of running tasks can be requested.
 */
class SupervisedTask<out T>(
    val name: String,
    private val job: suspend () -> T
) {

    /**
     * Internal supervisor
     */
    private val supervisor: CompletableJob = SupervisorJob()

    /**
     * An internal thread pool that uses all available system threads. This means that multiple jobs potentially all
     * use every available core, which may reduce throughput. However, this approach is favoured for its modularization.
     */
    private lateinit var threadPool: ExecutorService

    /**
     * Whether the job has already started
     */
    private var started = false

    /**
     * Code blocks that are executed once the job is complete
     */
    private val completionHandlers = mutableListOf<CompletionHandler>()

    /**
     * Invoke an [action] on completion of the task. The [CompletionHandler] must be registered before [start] is
     * called.
     */
    fun onCompletion(action: CompletionHandler) {
        if (started)
            throw IllegalStateException("supervised job already started")

        completionHandlers += action
    }

    /**
     * Start the long-running job and return a [Deferred] result
     */
    internal fun start(): Deferred<Result<T>> {
        if (started)
            throw IllegalStateException("supervised job already started")

        started = true

        logger()
            .debug("create fixed thread pool for \"$name\" with ${Runtime.getRuntime().availableProcessors()} threads")
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

        supervisor.invokeOnCompletion { t ->
            if (t != null) {
                logger().debug("job \"$name\" failed", t)
            } else {
                logger().debug("job \"$name\" completed")
            }

            this@SupervisedTask.completionHandlers.forEach { it.invoke(t) }

            logger().debug("shutdown fixed thread pool for $name...")
            threadPool.shutdown()
        }

        return CoroutineScope(threadPool.asCoroutineDispatcher()).async {
            try {
                val r = withContext(supervisor) {
                    job.invoke()
                }
                supervisor.complete()
                Result.success(r)
            } catch (t: Throwable) {
                logger().debug("supervised job \"$name\" crashed: ", t)
                supervisor.completeExceptionally(t)
                Result.failure(t)
            }
        }
    }
}