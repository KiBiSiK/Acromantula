package net.cydhra.acromantula

import kotlinx.coroutines.runBlocking
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.cli.CommandLineService
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.config.ConfigurationService
import net.cydhra.acromantula.plugins.PluginService
import net.cydhra.acromantula.rpc.RemoteProcedureService
import net.cydhra.acromantula.workspace.WorkspaceService

fun main() {
    runBlocking {
        EventBroker.initialize()
        ConfigurationService.initialize()
        WorkspaceService.initialize()
        CommandDispatcherService.initialize()
        RemoteProcedureService.initialize()
        CommandLineService.initialize()
        PluginService.initialize()

        RemoteProcedureService.onStartUp()
        CommandLineService.onStartUp()
    }
}