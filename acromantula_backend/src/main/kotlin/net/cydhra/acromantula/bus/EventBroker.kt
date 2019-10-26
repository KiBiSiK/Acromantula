package net.cydhra.acromantula.bus

import kotlinx.coroutines.*
import net.cydhra.acromantula.bus.event.Event
import net.cydhra.acromantula.bus.service.Service
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
    private val registeredEventHandlers = mutableMapOf<Service,
            MutableList<Pair<KClass<out Event>, suspend (Event) -> Unit>>>()

    private val singleThreadPool = Executors.newSingleThreadExecutor()

    private val singleThreadScope = CoroutineScope(singleThreadPool.asCoroutineDispatcher())

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
     * Any component may call this method to inform the bus and all services about an event. The event will be
     * dispatched to all services registered to the specific event type.
     *
     * @param event a serializable event instance that is then handled by services
     */
    fun fireEvent(event: Event) {
        val id = eventIdCounter.incrementAndGet()
        logger.trace("scheduling event (id: $id) $event")

        singleThreadScope.launch(eventExceptionHandler) {
            logger.trace("dispatch event with id: $id")

            // TODO
        }
    }

    /**
     * Register a listener function for a given event class. All events of the specified class are now
     */
    suspend fun <T : Event> registerEventListener(
        service: Service,
        eventClass: KClass<T>,
        listener: suspend (T) -> Unit
    ) {
        withContext(singleThreadContext) {
            @Suppress("UNCHECKED_CAST")
            registeredEventHandlers.getOrPut(service, { mutableListOf() })
                .add(Pair(eventClass, listener as (suspend (Event) -> Unit)))
        }
    }

    private suspend fun shutdown() {
        logger.info("shutdown event broker thread")
        singleThreadPool.shutdown()
    }
}