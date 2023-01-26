package net.cydhra.acromantula.workspace.filesystem

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import net.cydhra.acromantula.workspace.database.DatabaseClient
import net.cydhra.acromantula.workspace.disassembly.FileViewTable
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

/**
 * Event loop instance for database syncing
 */
internal object DatabaseSyncEventLoop : FileSystemEventLoop()

/**
 * An observer of all file system events that will synchronize them with the database in the background. To do this,
 * the observer adds all events to a flow of events which is handled by a long-running coroutine dispatched on the
 * IO-thread-pool. If the workspace closes, the flow is ended and the handler ends once all remaining data has been
 * synchronized with the database
 */
internal class FileSystemDatabaseSync(
    private val fs: WorkspaceFileSystem,
    private val databaseClient: DatabaseClient
) : FileSystemObserver by DatabaseSyncEventLoop {

    init {
        DatabaseSyncEventLoop.eventChannel.consumeAsFlow().buffer(256).onEach {
            LogManager.getLogger().trace("file system event: $it")
            handleEvent(it)
        }.onCompletion {
            LogManager.getLogger().trace("file system observer closing...")
        }.launchIn(CoroutineScope(Dispatchers.IO))
        LogManager.getLogger().trace("file system syncing is active")
    }

    @Suppress("REDUNDANT_ELSE_IN_WHEN") // crash if events are not implemented
    private fun handleEvent(event: FileSystemEvent) {
        when (event) {
            is FileSystemEvent.FileCreatedEvent -> syncNewFileIntoDatabase(event)
            is FileSystemEvent.FileDeletedEvent -> syncDeleteFileIntoDatabase(event)
            is FileSystemEvent.FileMovedEvent -> syncMoveFileIntoDatabase(event)
            is FileSystemEvent.FileRenamedEvent -> syncRenameFileIntoDatabase(event)
            is FileSystemEvent.FileUpdatedEvent -> Unit
            is FileSystemEvent.ArchiveCreatedEvent -> syncArchiveCreatedIntoDatabase(event)
            is FileSystemEvent.ViewCreatedEvent -> syncViewCreatedIntoDatabase(event)
            else -> TODO("missing event dispatch")
        }
    }

    private fun syncNewFileIntoDatabase(event: FileSystemEvent.FileCreatedEvent) {
        event.fileEntity.databaseId = databaseClient.transaction {
            FileTable.insertAndGetId {
                it[name] = event.fileEntity.name

                if (event.fileEntity.parent != null) {
                    val cachedId = event.fileEntity.parent!!.databaseId
                    it[parent] = cachedId
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

    private fun syncDeleteFileIntoDatabase(event: FileSystemEvent.FileDeletedEvent) {
        databaseClient.transaction {
            FileTable.deleteWhere { FileTable.id eq event.fileEntity.databaseId.value }
        }
    }

    private fun syncMoveFileIntoDatabase(event: FileSystemEvent.FileMovedEvent) {
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

    private fun syncRenameFileIntoDatabase(event: FileSystemEvent.FileRenamedEvent) {
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

    private fun syncArchiveCreatedIntoDatabase(event: FileSystemEvent.ArchiveCreatedEvent) {
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

    private fun syncViewCreatedIntoDatabase(event: FileSystemEvent.ViewCreatedEvent) {
        databaseClient.transaction {
            FileViewTable.insertAndGetId {
                it[file] = event.fileEntity.databaseId
                it[type] = event.viewEntity.type
                it[resource] = event.viewEntity.resource
                it[created] = DateTime(event.viewEntity.created)
            }
        }
    }
}
