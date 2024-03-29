package net.cydhra.acromantula

import net.cydhra.acromantula.cli.CommandLineService
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.config.ConfigurationService
import net.cydhra.acromantula.features.PrefabFeatures
import net.cydhra.acromantula.plugins.PluginService
import net.cydhra.acromantula.pool.TaskScheduler
import net.cydhra.acromantula.rpc.RemoteProcedureService
import net.cydhra.acromantula.workspace.WorkspaceService

fun main() {
    ConfigurationService.initialize()
    WorkspaceService.initialize()
    TaskScheduler.initialize()
    CommandDispatcherService.initialize()
    CommandLineService.initialize()
    PluginService.initialize()
    RemoteProcedureService.initialize()
    PrefabFeatures.initialize()

    CommandLineService.onStartUp()
    RemoteProcedureService.onStartUp()
}

/**
 * Called from command line or RPC clients to initiate a server shutdown.
 */
fun shutdownServer() {
    // shutdown from an external thread, to prevent a caller of this function to deadlock its own thread-pool
    object : Thread() {
        override fun run() {
            TaskScheduler.onShutdown()
            WorkspaceService.onShutdown()
            RemoteProcedureService.onShutdown()
            CommandLineService.onShutdown()
        }
    }.start()
}