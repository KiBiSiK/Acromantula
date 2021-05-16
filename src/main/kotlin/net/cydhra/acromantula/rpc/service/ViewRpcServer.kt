package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ExportViewCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ViewCommandInterpreter
import net.cydhra.acromantula.features.view.GenerateViewFeature
import net.cydhra.acromantula.proto.*
import net.cydhra.acromantula.workspace.WorkspaceService

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

    override suspend fun view(request: ViewCommand): ViewEntity {
        val result = CommandDispatcherService.dispatchCommandSupervised(
            when {
                request.fileId != -1 -> ViewCommandInterpreter(request.fileId, request.type)
                request.filePath != null -> ViewCommandInterpreter(request.filePath, request.type)
                else -> throw IllegalArgumentException("either fileId or filePath must be defined")
            }
        )

        result.onFailure { throw it }

        val view = result.getOrThrow() ?: throw java.lang.IllegalArgumentException("cannot generate view of given type")

        return viewEntity {
            id = view.id.value
            type = view.type
            url = WorkspaceService.getFileUrl(view.resource).toExternalForm()

        }
    }

    override suspend fun exportView(request: ExportViewCommand): Empty {
        val result = CommandDispatcherService.dispatchCommandSupervised(
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

        result.onFailure { throw it }

        return Empty.getDefaultInstance()
    }
}