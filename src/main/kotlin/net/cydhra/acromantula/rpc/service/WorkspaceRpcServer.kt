package net.cydhra.acromantula.rpc.service

import com.google.protobuf.ByteString
import com.google.protobuf.Empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.CreateFileCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.DeleteCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ListFilesCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.RenameFileCommandInterpreter
import net.cydhra.acromantula.proto.*
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.util.TreeNode
import org.jetbrains.exposed.sql.transactions.transaction

class WorkspaceRpcServer : WorkspaceServiceGrpcKt.WorkspaceServiceCoroutineImplBase() {

    private fun fileTreeToProto(treeNode: TreeNode<net.cydhra.acromantula.workspace.filesystem.FileEntity>): FileEntity =
        transaction {
            val children = treeNode.childList.map(::fileTreeToProto)
            fileEntity {
                id = treeNode.value.id.value
                name = treeNode.value.name
                isDirectory = treeNode.value.isDirectory

                if (treeNode.value.archiveEntity != null) {
                    archiveFormat = treeNode.value.archiveEntity!!.typeIdent
                }

                children(*children.toTypedArray())
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

    override fun showFile(request: ShowFileCommand): Flow<FileChunk> {
        val fileEntity = when (request.fileIdCase) {
            ShowFileCommand.FileIdCase.ID -> WorkspaceService.queryPath(request.id)
            ShowFileCommand.FileIdCase.PATH -> WorkspaceService.queryPath(
                WorkspaceService.queryPath(request.path).id.value
            )

            null, ShowFileCommand.FileIdCase.FILEID_NOT_SET -> throw MissingTargetFileException()
        }

        // TODO file size is not set correctly yet
        return WorkspaceService.getFileContent(fileEntity)
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
            .asFlow()
            .flowOn(Dispatchers.IO)
    }

    override suspend fun createFile(request: CreateFileCommand): FileEntity {
        val cmd = when (request.parentIdCase) {
            CreateFileCommand.ParentIdCase.ID -> CreateFileCommandInterpreter(
                request.id,
                request.name,
                request.isDirectory
            )

            CreateFileCommand.ParentIdCase.PATH -> CreateFileCommandInterpreter(
                request.path,
                request.name,
                request.isDirectory
            )

            null, CreateFileCommand.ParentIdCase.PARENTID_NOT_SET -> throw MissingTargetFileException()
        }

        val result = CommandDispatcherService.dispatchCommand("[RPC] create file", cmd).await()

        result.onFailure {
            throw it
        }

        // tree node is usually used by WorkspaceService.listFilesRecursively, but we know our new file has no
        // children, so we can use it here without constructing a tree
        return fileTreeToProto(TreeNode(result.getOrThrow()))
    }

    override suspend fun renameFile(request: RenameFileCommand): Empty {
        val command = when (request.fileIdCase) {
            RenameFileCommand.FileIdCase.ID -> RenameFileCommandInterpreter(request.id, request.newName)
            RenameFileCommand.FileIdCase.PATH -> RenameFileCommandInterpreter(request.path, request.newName)
            null, RenameFileCommand.FileIdCase.FILEID_NOT_SET -> throw MissingTargetFileException()
        }

        val result = CommandDispatcherService.dispatchCommand("[RPC] rename file", command).await()

        result.onFailure {
            throw it
        }

        return Empty.getDefaultInstance()
    }

    override suspend fun replaceFile(request: ReplaceFileCommand): Empty {
        val fileEntity = when (request.fileIdCase) {
            ReplaceFileCommand.FileIdCase.ID -> WorkspaceService.queryPath(request.id)
            ReplaceFileCommand.FileIdCase.PATH -> WorkspaceService.queryPath(request.path)
            null, ReplaceFileCommand.FileIdCase.FILEID_NOT_SET -> throw MissingTargetFileException()
        }

        // todo this should be done through a command interpreter (analogous to reconstructFile) and routed through
        //  the mapping feature for re-mapping
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