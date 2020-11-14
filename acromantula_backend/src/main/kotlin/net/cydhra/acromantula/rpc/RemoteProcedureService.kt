package net.cydhra.acromantula.rpc

import io.grpc.Server
import io.grpc.ServerBuilder
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.bus.events.ApplicationStartupEvent
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.*
import net.cydhra.acromantula.proto.*
import org.apache.logging.log4j.LogManager

/**
 * Server that translates remote procedure calls into command invocations.
 * TODO: report back command results to caller
 */
object RemoteProcedureService : RemoteDispatcherGrpcKt.RemoteDispatcherCoroutineImplBase(), Service {
    override val name: String = "RPC Server"

    private val logger = LogManager.getLogger()

    private lateinit var server: Server

    override suspend fun initialize() {
        logger.info("running RPC server...")

        EventBroker.registerEventListener(ApplicationStartupEvent::class, this::onStartUp)

        server = ServerBuilder.forPort(26666).addService(this).build()
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun onStartUp(@Suppress("UNUSED_PARAMETER") event: ApplicationStartupEvent) {
        server.start()
        logger.info("rpc service listening for clients...")
    }

    override suspend fun importFile(request: ImportCommand): ImportResponse {
        CommandDispatcherService.dispatchCommand(
            ImportCommandInterpreter(
                directory = request.directoryId,
                directoryPath = request.directoryPath,
                fileUrl = request.fileUrl
            )
        )
        return ImportResponse.newBuilder().build()
    }

    override suspend fun exportFile(request: ExportCommand): ExportResponse {
        CommandDispatcherService.dispatchCommand(
            ExportCommandInterpreter(
                fileEntityId = request.fileId,
                exporterName = request.exporter,
                targetFileName = request.targetPath
            )
        )
        return ExportResponse.newBuilder().build()
    }

    override suspend fun listFiles(request: ListFilesCommand): ListFilesResponse {
        CommandDispatcherService.dispatchCommand(
            ListFilesCommandInterpreter(
                directoryId = request.fileId,
                directoryPath = request.filePath
            )
        )
        return ListFilesResponse.newBuilder().build()
    }

    override suspend fun view(request: ViewCommand): ViewResponse {
        CommandDispatcherService.dispatchCommand(
            ViewCommandInterpreter(
                fileEntityId = request.fileId,
                type = request.type
            )
        )
        return ViewResponse.newBuilder().build()
    }

    override suspend fun exportView(request: ExportViewCommand): ExportViewResponse {
        CommandDispatcherService.dispatchCommand(
            ExportViewCommandInterpreter(
                fileEntityId = request.fileId,
                viewType = request.type,
                recursive = request.recursive,
                includeIncompatible = request.includeIncompatible,
                targetFileName = request.targetPath
            )
        )
        return ExportViewResponse.newBuilder().build()
    }
}