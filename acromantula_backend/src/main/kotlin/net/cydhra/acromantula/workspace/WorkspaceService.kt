package net.cydhra.acromantula.workspace

import net.cydhra.acromantula.bus.service.Service

/**
 * Facade service for the workspace sub-system. Everything related to data storage and data operation is delegated
 * from here.
 */
object WorkspaceService : Service {

    override val name: String = "workspace-service"

    private val pipeServer = PipeServer()

    /**
     * Called upon application startup. Load default workspace and subscribe to events if necessary.
     */
    override suspend fun initialize() {
        pipeServer.hostEndpoint()
    }

}