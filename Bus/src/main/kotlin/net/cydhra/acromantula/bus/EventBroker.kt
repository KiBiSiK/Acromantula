package net.cydhra.acromantula.bus

import com.google.common.collect.HashMultimap
import kotlinx.coroutines.*
import net.cydhra.acromantula.bus.events.ApplicationShutdownEvent
import org.apache.logging.log4j.LogManager
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

/**
 * Central event bus. This is the core of the application where all services are registered and all events are
 * dispatched. Components should not talk to each other but only to the broker using events. The broker
 * handles service and listener registration thread-safe and dispatches events in parallel.
 */
object EventBroker {
    private val logger = LogManager.getLogger()


    /**
     * A map that maps registered event listeners to their registering service. The listeners are mapped to their
     * services, so they are not called before the service is registered. The listeners are paired with a [KClass]
     * instance of the event class that is listened to.
     */
    private val registeredEventHandlers = HashMultimap.create<KClass<out Event>, suspend (Event) -> Unit>()

    private val singleThreadPool = Executors.newSingleThreadExecutor()

    private val singleThreadScope = CoroutineScope(singleThreadPool.asCoroutineDispatcher())

    /**
     * Work-stealing pool that is used for event handling
     */
    // uses a fork-join pool in async mode (ideal for small task bursts that are never joined). Threads block when no
    // tasks are available, freeing up the CPU for the heavy-duty worker threads of the worker pool
    private val eventHandlerPool = Executors.newWorkStealingPool()

    /**
     * Coroutine scope using the [eventHandlerPool]
     */
    private val eventHandlerScope = CoroutineScope(eventHandlerPool.asCoroutineDispatcher())

    /**
     * Single thread context for critical modifications of event handler list and service list
     */
    private val singleThreadContext = singleThreadScope.coroutineContext

    private val eventIdCounter = AtomicInteger()

    /**
     * Default handling for exceptions during event dispatch, so the logger reports any errors.
     */
    private val eventExceptionHandler = CoroutineExceptionHandler { _, exception ->
        logger.error("error during event dispatch", exception)
    }

    suspend fun initialize() {
        registerEventListener(ApplicationShutdownEvent::class, ::shutdown)
    }

    /**
     * Fire an event into the event bus. This method will not wait for event handling itself, but instead fire and
     * forget the event. After this method returns, the event may or may not be already handled.
     *
     * @param event an event that is fired and then not used again
     */
    fun fireEvent(event: Event) {
        eventHandlerScope.launch {
            val handlers = withContext(singleThreadContext) {
                registeredEventHandlers[event.javaClass.kotlin]
            }

            handlers.forEach { handler -> eventHandlerScope.launch { handler(event) } }
        }
    }

    /**
     * Register a listener function for a given event class. May suspend to synchronize
     */
    suspend fun <T : Event> registerEventListener(
        eventClass: KClass<T>,
        listener: suspend (T) -> Unit
    ) {
        withContext(singleThreadContext) {
            @Suppress("UNCHECKED_CAST")
            registeredEventHandlers.put(eventClass, listener as (suspend (Event) -> Unit))
        }
    }

    /**
     * Unregister an event listener that was previously registered.
     *
     * @param eventClass the event class that is being listened to
     * @param listener the reference to the listener that has been registered
     */
    suspend fun <T : Event> unregisterEventListener(
        eventClass: KClass<T>,
        listener: suspend (T) -> Unit
    ) {
        withContext(singleThreadContext) {
            registeredEventHandlers.remove(eventClass, listener)
        }
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun shutdown(@Suppress("UNUSED_PARAMETER") e: ApplicationShutdownEvent) {
        logger.info("shutdown event broker thread")
        singleThreadPool.shutdown()
    }
}