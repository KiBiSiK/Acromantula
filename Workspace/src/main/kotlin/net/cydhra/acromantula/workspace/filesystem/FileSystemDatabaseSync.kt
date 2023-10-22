package net.cydhra.acromantula.workspace.filesystem

import net.cydhra.acromantula.workspace.database.DatabaseClient
import net.cydhra.acromantula.workspace.disassembly.FileViewTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

/**
 * An observer of all file system events that will synchronize them with the database in the background. To do this,
 * the observer adds all events to a flow of events which is handled by a long-running coroutine dispatched on the
 * IO-thread-pool. If the workspace closes, the flow is ended and the handler ends once all remaining data has been
 * synchronized with the database
 */
internal class FileSystemDatabaseSync(
    private val fs: WorkspaceFileSystem,
    private val databaseClient: DatabaseClient
) : FileSystemObserver {


    override suspend fun onFileCreated(event: FileSystemEvent.FileCreatedEvent) {
        event.fileEntity.databaseId = databaseClient.transaction {
            FileTable.insertAndGetId {
                it[name] = event.fileEntity.name

                if (event.fileEntity.parent != null) {
                    while (true) {
                        try {
                            val cachedId = event.fileEntity.parent!!.databaseId
                            it[parent] = cachedId
                            break
                        } catch (e: UninitializedPropertyAccessException) {
                            println("trying to access ${event.fileEntity.parent!!.name} from ${event.fileEntity.name}")
                        }
                    }
                }

                it[isDirectory] = event.fileEntity.isDirectory
                it[type] = event.fileEntity.type // TODO: do not use strings, this is waste of space
                it[resource] = event.fileEntity.resource

                if (event.fileEntity.archiveType != null) {
                    it[archive] = EntityID(fs.getArchiveId(event.fileEntity.archiveType!!)!!, ArchiveTable)
                }
            }
        }
    }

    override suspend fun onFileUpdated(event: FileSystemEvent.FileUpdatedEvent) {}

    override suspend fun onFileRenamed(event: FileSystemEvent.FileRenamedEvent) {
        databaseClient.transaction {
            FileTable.update(
                where = {
                    FileTable.id eq event.fileEntity.databaseId.value
                },
                body = {
                    it[name] = event.fileEntity.name
                }
            )
        }
    }

    override suspend fun onFileMoved(event: FileSystemEvent.FileMovedEvent) {
        databaseClient.transaction {
            FileTable.update(
                where = {
                    FileTable.id eq event.fileEntity.databaseId.value
                },
                body = {
                    it[parent] = event.fileEntity.parent?.databaseId
                }
            )
        }
    }

    override suspend fun onFileDeleted(event: FileSystemEvent.FileDeletedEvent) {
        databaseClient.transaction {
            FileTable.deleteWhere { FileTable.id eq event.fileEntity.databaseId.value }
        }
    }

    override suspend fun onViewCreated(event: FileSystemEvent.ViewCreatedEvent) {
        event.viewEntity.databaseId = databaseClient.transaction {
            FileViewTable.insertAndGetId {
                it[file] = event.fileEntity.databaseId
                it[viewGenerator] = event.viewEntity.type
                it[resource] = event.viewEntity.resource
                it[created] = DateTime(event.viewEntity.created)
            }
        }
    }

    override suspend fun onArchiveCreated(event: FileSystemEvent.ArchiveCreatedEvent) {
        databaseClient.transaction {
            FileTable.update(
                where = {
                    FileTable.id eq event.fileEntity.databaseId.value
                },
                body = {
                    it[archive] = EntityID(fs.getArchiveId(event.fileEntity.archiveType!!)!!, ArchiveTable)
                }
            )
        }
    }

    override suspend fun onViewDeleted(event: FileSystemEvent.ViewDeletedEvent) {
        databaseClient.transaction {
            FileViewTable.deleteWhere {
                FileViewTable.id eq event.viewEntity.databaseId
            }
        }
    }
}
