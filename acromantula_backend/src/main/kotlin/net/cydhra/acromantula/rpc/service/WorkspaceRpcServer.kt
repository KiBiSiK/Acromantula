package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ListFilesCommandInterpreter
import net.cydhra.acromantula.proto.*
import net.cydhra.acromantula.workspace.WorkspaceService

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

    override suspend fun listFiles(request: ListFilesCommand): CommandResponse {
        val task = if (request.fileId != -1) {
            CommandDispatcherService.dispatchCommand(ListFilesCommandInterpreter(request.fileId))
        } else {
            CommandDispatcherService.dispatchCommand(ListFilesCommandInterpreter(request.filePath?.takeIf { it.isNotBlank() }))
        }

        return commandResponse {
            this.taskId = task.id
            this.taskStatus = task.status
        }
    }
}