package net.cydhra.acromantula.workspace.filesystem

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.apache.logging.log4j.LogManager

/**
 * An observer of all file system events that will synchronize them with the database in the background. To do this,
 * the observer adds all events to a flow of events which is handled by a long-running coroutine dispatched on the
 * IO-thread-pool. If the workspace closes, the flow is ended and the handler ends once all remaining data has been
 * synchronized with the database
 */
class FileSystemDatabaseSync : FileSystemObserver {

    private val eventChannel = Channel<FileSystemEvent>()

    init {
        eventChannel.consumeAsFlow().buffer(256).onEach {
                LogManager.getLogger().trace("file system event: $it")
            }.onCompletion {
                LogManager.getLogger().trace("file system observer closing...")
            }.launchIn(CoroutineScope(Dispatchers.IO))
        LogManager.getLogger().trace("file system syncing is active")
    }

    override suspend fun onFileCreated(event: FileSystemEvent.FileCreatedEvent) {
        eventChannel.send(event)
    }

    override suspend fun onFileUpdated(event: FileSystemEvent.FileUpdatedEvent) {
        eventChannel.send(event)
    }

    override suspend fun onFileRenamed(event: FileSystemEvent.FileRenamedEvent) {
        eventChannel.send(event)
    }

    override suspend fun onFileMoved(event: FileSystemEvent.FileMovedEvent) {
        eventChannel.send(event)
    }

    override suspend fun onFileDeleted(event: FileSystemEvent.FileDeletedEvent) {
        eventChannel.send(event)
    }

    override suspend fun onViewCreated(event: FileSystemEvent.ViewCreatedEvent) {
        eventChannel.send(event)
    }

    override suspend fun onArchiveCreated(event: FileSystemEvent.ArchiveCreatedEvent) {
        eventChannel.send(event)
    }


}