package net.cydhra.acromantula.rpc.service

import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ImportCommandInterpreter
import net.cydhra.acromantula.proto.CommandResponse
import net.cydhra.acromantula.proto.ImportCommand
import net.cydhra.acromantula.proto.ImportServiceGrpcKt
import net.cydhra.acromantula.proto.commandResponse

class ImportRpcServer : ImportServiceGrpcKt.ImportServiceCoroutineImplBase() {

    override suspend fun importFile(request: ImportCommand): CommandResponse {
        val task = CommandDispatcherService.dispatchCommand(
            when {
                request.directoryId != -1 ->
                    ImportCommandInterpreter(request.directoryId, request.fileUrl)
                request.directoryPath != null -> ImportCommandInterpreter(request.directoryPath, request.fileUrl)
                else -> throw IllegalArgumentException("either directoryId or directoryPath must be defined")
            }
        )
        return commandResponse {
            this.taskId = task.id
            this.taskStatus = task.status
        }
    }
}