package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.events.ApplicationStartupEvent
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.workspace.ipc.IPCServer

/**
 * Facade service for the workspace sub-system. Everything related to data storage and data operation is delegated
 * from here.
 */
object WorkspaceService : Service {

    override val name: String = "workspace-service"

    private val pipeServer = IPCServer()

    /**
     * Called upon application startup. Load default workspace and subscribe to events if necessary.
     */
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