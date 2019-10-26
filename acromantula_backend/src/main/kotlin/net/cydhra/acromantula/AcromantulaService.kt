package net.cydhra.acromantula

import kotlinx.coroutines.runBlocking
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.plugins.PluginService
import net.cydhra.acromantula.workspace.WorkspaceService

fun main() {
    runBlocking {
        EventBroker.registerService(EventBroker)
        EventBroker.registerService(PluginService)
        EventBroker.registerService(WorkspaceService)
    }
}