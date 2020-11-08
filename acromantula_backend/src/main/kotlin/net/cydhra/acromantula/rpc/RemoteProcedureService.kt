package net.cydhra.acromantula.rpc

import com.google.protobuf.RpcCallback
import com.google.protobuf.RpcController
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.proto.*
import org.apache.logging.log4j.LogManager

object RemoteProcedureService : RemoteDispatcher(), Service {
    override val name: String = "RPC Server"

    private val logger = LogManager.getLogger()

    override suspend fun initialize() {
        logger.info("running RPC server...")
    }

    override fun importFile(controller: RpcController?, request: ImportCommand?, done: RpcCallback<ImportResponse>?) {
        TODO("not implemented")
    }

    override fun exportFile(controller: RpcController?, request: ExportCommand?, done: RpcCallback<ExportResponse>?) {
        TODO("not implemented")
    }

    override fun listFiles(
        controller: RpcController?,
        request: ListFilesCommand?,
        done: RpcCallback<ListFilesResponse>?
    ) {
        TODO("not implemented")
    }

    override fun view(controller: RpcController?, request: ViewCommand?, done: RpcCallback<ViewResponse>?) {
        TODO("not implemented")
    }

    override fun exportView(
        controller: RpcController?,
        request: ExportViewCommand?,
        done: RpcCallback<ExportViewResponse>?
    ) {
        TODO("not implemented")
    }

}