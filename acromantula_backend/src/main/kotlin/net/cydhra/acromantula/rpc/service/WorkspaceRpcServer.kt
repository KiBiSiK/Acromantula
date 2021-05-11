package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ListFilesCommandInterpreter
import net.cydhra.acromantula.proto.*
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.disassembly.FileRepresentation
import net.cydhra.acromantula.workspace.util.TreeNode

class WorkspaceRpcServer : WorkspaceServiceGrpcKt.WorkspaceServiceCoroutineImplBase() {
    override suspend fun listTasks(request: Empty): TaskListResponse {
        return taskListResponse {
            this.tasks = WorkspaceService.getWorkerPool()
                .listTasks()
                .map { task ->
                    task {
                        this.taskId = task.id
                        this.taskStatus = task.status
                        this.finished = task.finished
                    }
                }
        }
    }

    override suspend fun listFiles(request: ListFilesCommand): ListFilesResponse {
        val result =
            if (request.fileId != -1) {
                CommandDispatcherService.dispatchCommand(ListFilesCommandInterpreter(request.fileId))
            } else {
                CommandDispatcherService.dispatchCommand(ListFilesCommandInterpreter(request.filePath?.takeIf { it.isNotBlank() }))
            }.await()

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
                views(*treeNode.value.views.map(::viewToProto).toTypedArray())
            }
        }

        val entries = result.getOrElse { throw it }
        return listFilesResponse {
            trees(*entries.map(::mapResultToProto).toTypedArray())
        }
    }
}