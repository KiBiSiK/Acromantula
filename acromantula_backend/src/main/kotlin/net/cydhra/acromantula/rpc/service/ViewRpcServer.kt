package net.cydhra.acromantula.rpc.service

import io.grpc.stub.StreamObserver
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ExportViewCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ViewCommandInterpreter
import net.cydhra.acromantula.proto.*

class ViewRpcServer : ViewServiceGrpc.ViewServiceImplBase() {
    override fun view(request: ViewCommand, responseObserver: StreamObserver<CommandResponse>) {
        val task = CommandDispatcherService.dispatchCommand(
            when {
                request.fileId != -1 -> ViewCommandInterpreter(request.fileId, request.type)
                request.filePath != null -> ViewCommandInterpreter(request.filePath, request.type)
                else -> throw IllegalArgumentException("either fileId or filePath must be defined")
            }
        )
        responseObserver.onNext(commandResponse {
            this.taskId = task.id
            this.taskStatus = task.status
        })
    }

    override fun exportView(request: ExportViewCommand, responseObserver: StreamObserver<CommandResponse>) {
        val task = CommandDispatcherService.dispatchCommand(
            when {
                request.fileId != -1 -> ExportViewCommandInterpreter(
                    request.fileId, request.type, request.recursive,
                    request.includeIncompatible, request.targetPath
                )
                request.filePath != null -> ExportViewCommandInterpreter(
                    request.filePath, request.type, request.recursive,
                    request.includeIncompatible, request.targetPath
                )
                else -> throw IllegalArgumentException("either fileId or filePath must be defined")
            }
        )
        responseObserver.onNext(commandResponse {
            this.taskId = task.id
            this.taskStatus = task.status
        })
    }
}