package net.cydhra.acromantula.bus.service

/**
 * Since the application is designed as an event-driven bus architecture, all sub systems are implemented as a
 * service that is initialized on startup and then served with events it subscribed to. All services implement this
 * interface and are registered at the event bus.
 */
interface Service {

    /**
     * Service friendly name
     */
    val name: String

    /**
     * Called before the service is registered at the event bus. No events can be received up to this point. This
     * method can be used to register to event types.
     */
    suspend fun initialize()
}