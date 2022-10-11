package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.DeleteCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ListFilesCommandInterpreter
import net.cydhra.acromantula.proto.*
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.disassembly.FileRepresentation
import net.cydhra.acromantula.workspace.util.TreeNode

class WorkspaceRpcServer : WorkspaceServiceGrpcKt.WorkspaceServiceCoroutineImplBase() {

    private fun viewToProto(view: FileRepresentation): ViewEntity {
        return viewEntity {
            id = view.id.value
            type = view.type
            url = WorkspaceService.getFileUrl(view.resource).toExternalForm()
        }
    }

    private fun fileTreeToProto(treeNode: TreeNode<net.cydhra.acromantula.workspace.filesystem.FileEntity>): FileEntity {
        val children = treeNode.childList.map(::fileTreeToProto)
        return fileEntity {
            id = treeNode.value.id.value
            name = treeNode.value.name
            isDirectory = treeNode.value.isDirectory
            children(*children.toTypedArray())
            views(*treeNode.value.getViews().map(::viewToProto).toTypedArray())
        }
    }

    override suspend fun listFiles(request: ListFilesCommand): ListFilesResponse {
        val interpreter = when (request.fileIdCase) {
            ListFilesCommand.FileIdCase.ID ->
                ListFilesCommandInterpreter(request.id)

            ListFilesCommand.FileIdCase.FILEPATH ->
                ListFilesCommandInterpreter(request.filePath?.takeIf { it.isNotBlank() })

            null, ListFilesCommand.FileIdCase.FILEID_NOT_SET -> throw MissingTargetFileException()
        }

        val result = CommandDispatcherService.dispatchCommand("[RPC] list files", interpreter).await()

        val entries = result.getOrElse { throw it }
        return listFilesResponse {
            trees(*entries.map(::fileTreeToProto).toTypedArray())
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

    override suspend fun createFile(request: CreateFileCommand): FileEntity {
        val parentEntity = when (request.parentIdCase) {
            CreateFileCommand.ParentIdCase.ID -> WorkspaceService.queryPath(request.id)
            CreateFileCommand.ParentIdCase.PATH ->
                if (request.path.isNotBlank())
                    WorkspaceService.queryPath(request.path)
                else
                    null

            null, CreateFileCommand.ParentIdCase.PARENTID_NOT_SET -> null
        }

        val newFile = if (request.isDirectory) {
            WorkspaceService.addFileEntry(request.name, parentEntity, ByteArray(0))
        } else {
            WorkspaceService.addDirectoryEntry(request.name, parentEntity)
        }

        // tree node is usually used by WorkspaceService.listFilesRecursively, but we know our new file has no
        // children, so we can use it here without constructing a tree
        return fileTreeToProto(TreeNode(newFile))
    }

    override suspend fun replaceFile(request: ReplaceFileCommand): Empty {
        val fileEntity = when (request.fileIdCase) {
            ReplaceFileCommand.FileIdCase.ID -> WorkspaceService.queryPath(request.id)
            ReplaceFileCommand.FileIdCase.PATH -> WorkspaceService.queryPath(request.path)
            null, ReplaceFileCommand.FileIdCase.FILEID_NOT_SET -> throw MissingTargetFileException()
        }

        WorkspaceService.updateFileEntry(fileEntity, request.newContent.toByteArray())
        return Empty.getDefaultInstance()
    }

    override suspend fun deleteFile(request: DeleteFileCommand): Empty {
        val cmd = when (request.fileIdCase) {
            DeleteFileCommand.FileIdCase.ID -> DeleteCommandInterpreter(request.id)
            DeleteFileCommand.FileIdCase.PATH -> DeleteCommandInterpreter(request.path)
            null, DeleteFileCommand.FileIdCase.FILEID_NOT_SET -> throw MissingTargetFileException()
        }

        val result = CommandDispatcherService.dispatchCommand("[RPC] delete file", cmd).await()
        result.onFailure {
            throw it
        }

        return Empty.getDefaultInstance()
    }
}