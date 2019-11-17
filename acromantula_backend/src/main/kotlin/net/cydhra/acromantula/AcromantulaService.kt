package net.cydhra.acromantula

import kotlinx.coroutines.runBlocking
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.events.ApplicationStartupEvent
import net.cydhra.acromantula.config.ConfigurationService
import net.cydhra.acromantula.plugins.PluginService
import net.cydhra.acromantula.workspace.WorkspaceService

fun main() {
    runBlocking {
        EventBroker.registerService(EventBroker)
        EventBroker.registerService(ConfigurationService)
        EventBroker.registerService(PluginService)
        EventBroker.registerService(WorkspaceService)

        EventBroker.fireEvent(ApplicationStartupEvent())
    }
}