package net.cydhra.acromantula.rpc.service

import com.google.protobuf.ByteString
import com.google.protobuf.Empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    override fun view(request: ViewCommand): Flow<FileChunk> = flow {
        val result = CommandDispatcherService.dispatchCommand(
            "[RPC] view $request",
            when (request.fileIdCase) {
                ViewCommand.FileIdCase.ID -> ViewCommandInterpreter(request.id, request.type.name)
                ViewCommand.FileIdCase.FILEPATH -> ViewCommandInterpreter(request.filePath, request.type.name)
                null, ViewCommand.FileIdCase.FILEID_NOT_SET -> throw MissingTargetFileException()
            }
        ).await()

        result.onFailure { throw it }

        // TODO there is error handling here that is supposed to be already handled. Look at GenerateViewFeature
        //  .generateView for more info
        val view = result.getOrThrow() ?: throw java.lang.IllegalArgumentException("cannot generate view of given type")
        WorkspaceService.getRepresentationContent(view)
            .buffered()
            .iterator()
            .asSequence()
            .chunked(request.chunkSize)
            .map {
                FileChunk.newBuilder()
                    .setTotalBytes(-1)
                    .setContent(ByteString.copyFrom(it.toTypedArray().toByteArray()))
                    .build()
            }
            .forEach { emit(it) }
    }.flowOn(Dispatchers.IO)

    override suspend fun exportView(request: ExportViewCommand): Empty {
        val result = CommandDispatcherService.dispatchCommand(
            "[RPC] $request",
            when (request.fileIdCase) {
                ExportViewCommand.FileIdCase.ID -> ExportViewCommandInterpreter(
                    request.id, request.type, request.recursive,
                    request.includeIncompatible, request.targetPath
                )

                ExportViewCommand.FileIdCase.FILEPATH -> ExportViewCommandInterpreter(
                    request.filePath, request.type, request.recursive,
                    request.includeIncompatible, request.targetPath
                )

                null, ExportViewCommand.FileIdCase.FILEID_NOT_SET -> throw MissingTargetFileException()
            }
        ).await()

        result.onFailure { throw it }

        return Empty.getDefaultInstance()
    }
}