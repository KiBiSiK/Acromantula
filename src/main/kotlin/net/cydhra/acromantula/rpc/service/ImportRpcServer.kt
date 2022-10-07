package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ImportCommandInterpreter
import net.cydhra.acromantula.proto.ImportCommand
import net.cydhra.acromantula.proto.ImportServiceGrpcKt

class ImportRpcServer : ImportServiceGrpcKt.ImportServiceCoroutineImplBase() {

    override suspend fun importFile(request: ImportCommand): Empty {
        val result = CommandDispatcherService.dispatchCommand(
            "[RPC] import ${request.fileUrl}",
            when {
                request.directoryId != -1 ->
                    ImportCommandInterpreter(request.directoryId, request.fileUrl)
                request.directoryPath != null -> ImportCommandInterpreter(request.directoryPath, request.fileUrl)
                else -> ImportCommandInterpreter(null as? String?, request.fileUrl)
            }
        ).await()

        result.onFailure {
            throw it
        }

        return Empty.getDefaultInstance()
    }
}