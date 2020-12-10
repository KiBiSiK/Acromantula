package net.cydhra.acromantula.rpc

import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import kotlinx.coroutines.runBlocking
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.events.ApplicationStartupEvent
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.proto.ImportServiceClient
import net.cydhra.acromantula.proto.importCommand
import net.cydhra.acromantula.rpc.service.ImportRpcServer
import net.cydhra.acromantula.workspace.WorkspaceService
import org.junit.jupiter.api.Test

const val PORT = 26262

class ImportServerTest {

    @Test
    fun testImportServer() {
        runBlocking {
            EventBroker.registerService(EventBroker)
            EventBroker.registerService(WorkspaceService)
            EventBroker.registerService(CommandDispatcherService)
            EventBroker.fireEvent(ApplicationStartupEvent())

            ServerBuilder.forPort(PORT).addService(ImportRpcServer()).build()

            val client = ImportServiceClient.create(
                channel = ManagedChannelBuilder.forAddress("localhost", 26666)
                    .usePlaintext()
                    .build()
            )
            val request = client.importFile(
                importCommand {
                    directoryId = -1
                }
            )

            println(request)

            client.shutdownChannel()
        }
    }
}
