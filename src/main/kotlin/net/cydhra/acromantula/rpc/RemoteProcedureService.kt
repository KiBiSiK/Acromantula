package net.cydhra.acromantula.rpc

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import net.cydhra.acromantula.rpc.service.*
import org.apache.logging.log4j.LogManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Server that translates remote procedure calls into command invocations.
 * TODO: report back command results to caller
 */
object RemoteProcedureService {
    private val logger = LogManager.getLogger()

    /**
     * Thread pool for the rpc server
     */
    private val executor = Executors.newCachedThreadPool()

    private lateinit var server: Server

    fun initialize() {
        logger.info("running RPC server...")

        server = ServerBuilder.forPort(26666)
            .executor(this.executor)
            .addService(BusRpcServer())
            .addService(WorkspaceRpcServer())
            .addService(ImportRpcServer())
            .addService(ExportRpcServer())
            .addService(ViewRpcServer())
            .addService(ProtoReflectionService.newInstance())
            .build()
    }

    fun onStartUp() {
        server.start()
        logger.info("rpc service listening for clients...")
    }

    fun onShutdown() {
        logger.info("shut down rpc server and its thread pool (timeout 60 seconds...)")
        server.shutdown()
        executor.shutdown()
        if (!server.awaitTermination(60L, TimeUnit.SECONDS)) {
            logger.warn("rpc server refuses to shutdown. Shutting down forcefully")
            server.shutdownNow()
        }
        logger.info("rpc server terminated.")

        if (!executor.awaitTermination(60L, TimeUnit.SECONDS)) {
            logger.warn("rpc thread pool refuses to shutdown. Shutting down forcefully")
            executor.shutdownNow()
        }
        logger.info("rpc threadpool terminated.")
    }
}