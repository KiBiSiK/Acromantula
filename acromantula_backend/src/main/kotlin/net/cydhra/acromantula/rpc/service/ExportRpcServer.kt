package net.cydhra.acromantula.rpc.service

import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ExportCommandInterpreter
import net.cydhra.acromantula.proto.CommandResponse
import net.cydhra.acromantula.proto.ExportCommand
import net.cydhra.acromantula.proto.ExportServiceGrpcKt
import net.cydhra.acromantula.proto.commandResponse

class ExportRpcServer : ExportServiceGrpcKt.ExportServiceCoroutineImplBase() {
    override suspend fun exportFile(request: ExportCommand): CommandResponse {
        val task = CommandDispatcherService.dispatchCommand(
            when {
                request.fileId != -1 -> ExportCommandInterpreter(request.fileId, request.exporter, request.targetPath)
                request.filePath != null -> ExportCommandInterpreter(
                    request.filePath,
                    request.exporter,
                    request.targetPath
                )
                else -> throw IllegalArgumentException("either fileId or filePath must be defined")
            }
        )
        return commandResponse {
            this.taskId = task.id
            this.taskStatus = task.status
        }
    }
}