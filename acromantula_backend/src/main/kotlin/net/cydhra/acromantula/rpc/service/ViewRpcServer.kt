package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ExportViewCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ViewCommandInterpreter
import net.cydhra.acromantula.features.view.GenerateViewFeature
import net.cydhra.acromantula.proto.*

class ViewRpcServer : ViewServiceGrpcKt.ViewServiceCoroutineImplBase() {

    override suspend fun getViewTypes(request: Empty): ViewTypes {
        return viewTypes {
            this.types = GenerateViewFeature.getViewTypes().map { (type, fileType) ->
                viewType {
                    this.name = type
                    this.generatedType = fileType
                }
            }
        }
    }

    override suspend fun view(request: ViewCommand): CommandResponse {
        val task = CommandDispatcherService.dispatchCommand(
            when {
                request.fileId != -1 -> ViewCommandInterpreter(request.fileId, request.type)
                request.filePath != null -> ViewCommandInterpreter(request.filePath, request.type)
                else -> throw IllegalArgumentException("either fileId or filePath must be defined")
            }
        )
        return commandResponse {
            this.taskId = task.id
            this.taskStatus = task.status
        }
    }

    override suspend fun exportView(request: ExportViewCommand): CommandResponse {
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

        return commandResponse {
            this.taskId = task.id
            this.taskStatus = task.status
        }
    }
}