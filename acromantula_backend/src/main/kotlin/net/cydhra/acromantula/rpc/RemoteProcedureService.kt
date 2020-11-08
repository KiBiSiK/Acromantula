package net.cydhra.acromantula.rpc

import com.google.protobuf.RpcCallback
import com.google.protobuf.RpcController
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.*
import net.cydhra.acromantula.proto.*
import org.apache.logging.log4j.LogManager

/**
 * Server that translates remote procedure calls into command invocations.
 * TODO: report back command results to caller
 */
object RemoteProcedureService : RemoteDispatcher(), Service {
    override val name: String = "RPC Server"

    private val logger = LogManager.getLogger()

    override suspend fun initialize() {
        logger.info("running RPC server...")
    }

    override fun importFile(controller: RpcController, request: ImportCommand, done: RpcCallback<ImportResponse>) {
        CommandDispatcherService.dispatchCommand(
            ImportCommandInterpreter(
                directory = request.directoryId,
                directoryPath = request.directoryPath,
                fileUrl = request.fileUrl
            )
        )
    }

    override fun exportFile(controller: RpcController, request: ExportCommand, done: RpcCallback<ExportResponse>) {
        CommandDispatcherService.dispatchCommand(
            ExportCommandInterpreter(
                fileEntityId = request.fileId,
                exporterName = request.exporter,
                targetFileName = request.targetPath
            )
        )
    }

    override fun listFiles(
        controller: RpcController,
        request: ListFilesCommand,
        done: RpcCallback<ListFilesResponse>
    ) {
        CommandDispatcherService.dispatchCommand(
            ListFilesCommandInterpreter(
                directoryId = request.fileId,
                directoryPath = request.filePath
            )
        )
    }

    override fun view(controller: RpcController, request: ViewCommand, done: RpcCallback<ViewResponse>) {
        CommandDispatcherService.dispatchCommand(
            ViewCommandInterpreter(
                fileEntityId = request.fileId,
                type = request.type
            )
        )
    }

    override fun exportView(
        controller: RpcController,
        request: ExportViewCommand,
        done: RpcCallback<ExportViewResponse>
    ) {
        CommandDispatcherService.dispatchCommand(
            ExportViewCommandInterpreter(
                fileEntityId = request.fileId,
                viewType = request.type,
                recursive = request.recursive,
                includeIncompatible = request.includeIncompatible,
                targetFileName = request.targetPath
            )
        )
    }

}