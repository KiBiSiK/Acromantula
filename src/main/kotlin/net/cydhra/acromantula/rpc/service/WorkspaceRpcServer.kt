package net.cydhra.acromantula.rpc.service

import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ListFilesCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ViewCommandInterpreter
import net.cydhra.acromantula.proto.*
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.disassembly.FileRepresentation
import net.cydhra.acromantula.workspace.util.TreeNode

class WorkspaceRpcServer : WorkspaceServiceGrpcKt.WorkspaceServiceCoroutineImplBase() {

    override suspend fun listFiles(request: ListFilesCommand): ListFilesResponse {

        val result =
            if (request.fileId != -1) {
                CommandDispatcherService.dispatchCommand(
                    "[RPC] list files",
                    ListFilesCommandInterpreter(request.fileId)
                )
                    .await()
            } else {
                CommandDispatcherService
                    .dispatchCommand(
                        "[RPC] list files",
                        ListFilesCommandInterpreter(request.filePath?.takeIf { it.isNotBlank() })
                    ).await()
            }

        fun viewToProto(view: FileRepresentation): ViewEntity {
            return viewEntity {
                id = view.id.value
                type = view.type
                url = WorkspaceService.getFileUrl(view.resource).toExternalForm()
            }
        }

        fun mapResultToProto(treeNode: TreeNode<net.cydhra.acromantula.workspace.filesystem.FileEntity>): FileEntity {
            val children = treeNode.childList.map(::mapResultToProto)
            return fileEntity {
                id = treeNode.value.id.value
                name = treeNode.value.name
                isDirectory = treeNode.value.isDirectory
                children(*children.toTypedArray())
                views(*treeNode.value.getViews().map(::viewToProto).toTypedArray())
            }
        }

        val entries = result.getOrElse { throw it }
        return listFilesResponse {
            trees(*entries.map(::mapResultToProto).toTypedArray())
        }
    }

    override suspend fun showFile(request: ShowFileCommand): ShowFileResponse {
        val fileUrl = when (request.fileIdCase) {
            ShowFileCommand.FileIdCase.ID -> WorkspaceService.getFileUrl(request.id)
            ShowFileCommand.FileIdCase.PATH -> WorkspaceService.getFileUrl(
                WorkspaceService.queryPath(request.path).id.value
            )

            null, ShowFileCommand.FileIdCase.FILEID_NOT_SET ->
                throw IllegalArgumentException("missing either file id or file path as unique identifier")
        }
        return ShowFileResponse.newBuilder().setUrl(fileUrl.toExternalForm()).build()
    }

    override suspend fun showView(request: ShowViewCommand): ShowViewResponse {
        val fileId = when (request.fileIdCase) {
            ShowViewCommand.FileIdCase.ID -> request.id
            ShowViewCommand.FileIdCase.PATH -> WorkspaceService.queryPath(request.path).id.value
            null, ShowViewCommand.FileIdCase.FILEID_NOT_SET ->
                throw IllegalArgumentException("missing either file id or file path as unique identifier")
        }

        val viewRepresentation = CommandDispatcherService.dispatchCommand(
            "[RPC View File]",
            ViewCommandInterpreter(fileId, request.viewType)
        ).await().getOrNull() ?: throw IllegalArgumentException("file type cannot be viewed as \"${request.viewType}\"")

        return ShowViewResponse.newBuilder()
            .setUrl(WorkspaceService.getRepresentationUrl(viewRepresentation.resource).toExternalForm())
            .build()
    }
}