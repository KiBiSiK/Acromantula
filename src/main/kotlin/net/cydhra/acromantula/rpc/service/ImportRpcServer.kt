package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ImportCommandInterpreter
import net.cydhra.acromantula.proto.ImportCommand
import net.cydhra.acromantula.proto.ImportServiceGrpcKt

class ImportRpcServer : ImportServiceGrpcKt.ImportServiceCoroutineImplBase() {

    override suspend fun importFile(request: ImportCommand): Empty {
        val result = CommandDispatcherService.dispatchCommand(
            when {
                request.directoryId != -1 ->
                    ImportCommandInterpreter(request.directoryId, request.fileUrl)
                request.directoryPath != null -> ImportCommandInterpreter(request.directoryPath, request.fileUrl)
                else -> throw IllegalArgumentException("either directoryId or directoryPath must be defined")
            }
        ).await()

        result.onFailure {
            throw it
        }

        return Empty.getDefaultInstance()
    }
}