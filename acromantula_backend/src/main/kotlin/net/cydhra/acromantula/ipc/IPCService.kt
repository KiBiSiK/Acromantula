package net.cydhra.acromantula.ipc

import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.bus.events.ApplicationStartupEvent

/**
 * This service handles IPC with frontend applications. It will forward events from clients to the
 * [net.cydhra.acromantula.bus.EventBroker] and proxy channel subscriptions of the client.
 */
object IPCService : Service {
    override val name: String = "ipc-service"

    /**
     * A server for communication with frontend applications. This is independent of, and exceeds the current workspace
     * connection.
     */
    private val pipeServer = IPCServer()

    override suspend fun initialize() {
        EventBroker.registerEventListener(ApplicationStartupEvent::class, this::onStartup)
    }

    @Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
    private suspend fun onStartup(event: ApplicationStartupEvent) {
        pipeServer.hostEndpoint()
    }

    /**
     * Shutdown routine that closes all resources and cleans up.
     */
    fun shutdown() {
        pipeServer.shutdown()
    }
}