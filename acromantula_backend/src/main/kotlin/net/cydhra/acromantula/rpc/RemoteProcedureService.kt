package net.cydhra.acromantula.rpc

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.bus.events.ApplicationStartupEvent
import net.cydhra.acromantula.rpc.service.ImportRpcServer
import org.apache.logging.log4j.LogManager

/**
 * Server that translates remote procedure calls into command invocations.
 * TODO: report back command results to caller
 */
object RemoteProcedureService : Service {
    override val name: String = "RPC Server"

    private val logger = LogManager.getLogger()

    private lateinit var server: Server

    override suspend fun initialize() {
        logger.info("running RPC server...")

        EventBroker.registerEventListener(ApplicationStartupEvent::class, this::onStartUp)

        server = ServerBuilder.forPort(26666)
            .addService(ImportRpcServer())
            .addService(ProtoReflectionService.newInstance())
            .build()
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun onStartUp(@Suppress("UNUSED_PARAMETER") event: ApplicationStartupEvent) {
        server.start()
        logger.info("rpc service listening for clients...")
    }
}