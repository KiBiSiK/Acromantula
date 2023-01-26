package net.cydhra.acromantula.workspace.filesystem

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insertAndGetId

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
internal class FileSystemDatabaseSync(private val fs: WorkspaceFileSystem) :
    FileSystemObserver by DatabaseSyncEventLoop {

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
            is FileSystemEvent.ArchiveCreatedEvent -> TODO()
            is FileSystemEvent.FileDeletedEvent -> TODO()
            is FileSystemEvent.FileMovedEvent -> TODO()
            is FileSystemEvent.FileRenamedEvent -> TODO()
            is FileSystemEvent.FileUpdatedEvent -> TODO()
            is FileSystemEvent.ViewCreatedEvent -> TODO()
            else -> TODO("missing event dispatch")
        }
    }

    private fun syncNewFileIntoDatabase(event: FileSystemEvent.FileCreatedEvent) {
        val id = FileTable.insertAndGetId {
            it[name] = event.fileEntity.name

            if (event.fileEntity.parent != null) {
                val cachedId = event.fileEntity.parent!!.databaseId
                    ?: throw IllegalStateException("cannot insert child before parent")
                it[parent] = cachedId
            }

            it[isDirectory] = event.fileEntity.isDirectory
            it[type] = event.fileEntity.type // TODO: do not use strings, this is waste of space
            it[resource] = event.fileEntity.resource

            if (event.fileEntity.archiveType != null) {
                it[archive] = EntityID(fs.getArchiveId(event.fileEntity.archiveType!!)!!, ArchiveTable)
            }
        }
        event.fileEntity.databaseId = id
    }
}