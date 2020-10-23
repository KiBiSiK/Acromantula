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
object EventBroker : Service {
    override val name: String = "event-broker"

    private val logger = LogManager.getLogger()

    /**
     * A list of all services that are registered at the application
     */
    private val registeredServices = mutableListOf<Service>()

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

    override suspend fun initialize() {
        registerEventListener(ApplicationShutdownEvent::class, ::shutdown)
    }

    /**
     * Register a new service at the system. The service is initialized before it is registered. As soon as it is
     * registered, event dispatching considers the service.
     */
    suspend fun registerService(service: Service) {
        logger.debug("attempting to register service: ${service.name}")
        service.initialize()

        withContext(singleThreadContext) {
            try {
                registeredServices += service
                logger.info("registered service: ${service.name}")
            } catch (t: Throwable) {
                logger.error("error during service registration", t)
            }
        }
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

    @Suppress("RedundantSuspendModifier")
    private suspend fun shutdown(@Suppress("UNUSED_PARAMETER") e: ApplicationShutdownEvent) {
        logger.info("shutdown event broker thread")
        singleThreadPool.shutdown()
    }
}