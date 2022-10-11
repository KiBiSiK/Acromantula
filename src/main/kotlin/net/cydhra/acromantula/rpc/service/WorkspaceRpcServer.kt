package net.cydhra.acromantula.rpc.service

import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ListFilesCommandInterpreter
import net.cydhra.acromantula.proto.*
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.disassembly.FileRepresentation
import net.cydhra.acromantula.workspace.util.TreeNode

class WorkspaceRpcServer : WorkspaceServiceGrpcKt.WorkspaceServiceCoroutineImplBase() {

    override suspend fun listFiles(request: ListFilesCommand): ListFilesResponse {

        val interpreter = when (request.fileIdCase) {
            ListFilesCommand.FileIdCase.ID ->
                ListFilesCommandInterpreter(request.id)

            ListFilesCommand.FileIdCase.FILEPATH ->
                ListFilesCommandInterpreter(request.filePath?.takeIf { it.isNotBlank() })

            null, ListFilesCommand.FileIdCase.FILEID_NOT_SET -> throw MissingTargetFileException()
        }

        val result = CommandDispatcherService.dispatchCommand("[RPC] list files", interpreter).await()

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

            null, ShowFileCommand.FileIdCase.FILEID_NOT_SET -> throw MissingTargetFileException()
        }
        return ShowFileResponse.newBuilder().setUrl(fileUrl.toExternalForm()).build()
    }
}