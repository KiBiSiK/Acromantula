package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ListFilesCommandInterpreter
import net.cydhra.acromantula.proto.*
import net.cydhra.acromantula.workspace.WorkspaceService
import net.cydhra.acromantula.workspace.util.TreeNode
import org.apache.logging.log4j.LogManager

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

    @ExperimentalCoroutinesApi
    override fun listFiles(request: ListFilesCommand): Flow<ListFilesResponse> {
        val task = if (request.fileId != -1) {
            CommandDispatcherService.dispatchCommand(ListFilesCommandInterpreter(request.fileId))
        } else {
            CommandDispatcherService.dispatchCommand(ListFilesCommandInterpreter(request.filePath?.takeIf { it.isNotBlank() }))
        }

        return callbackFlow {
            val taskStatusChangedListener: suspend (TaskStatusChangedEvent) -> Unit = { event ->
                if (event.taskId == task.id) {
                    fun mapResultToProto(treeNode: TreeNode<FileEntity>): ProtoFileEntity {
                        val children = treeNode.childList.map(::mapResultToProto)
                        return fileEntity {
                            id = treeNode.value.id.value
                            name = treeNode.value.name
                            isDirectory = treeNode.value.isDirectory
                            children(*children.toTypedArray())
                        }
                    }

                    @Suppress("UNCHECKED_CAST")
                    val result = (WorkspaceService.getWorkerPool().reap(event.taskId) as
                            Task<List<TreeNode<FileEntity>>>).result.get();

                    result.onSuccess {
                        offer(
                            listFilesResponse {
                                trees(*it.map(::mapResultToProto).toTypedArray())
                            }
                        )
                        close()
                    }
                    result.onFailure { t ->
                        close(cause = t)
                    }
                }
            }

            EventBroker.registerEventListener(TaskStatusChangedEvent::class, taskStatusChangedListener)

            awaitClose {
                runBlocking {
                    EventBroker.unregisterEventListener(TaskStatusChangedEvent::class, taskStatusChangedListener)
                }
            }
        }
    }
}