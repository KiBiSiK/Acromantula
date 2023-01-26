package net.cydhra.acromantula.workspace.filesystem

import kotlinx.coroutines.channels.Channel

/**
 * An observer of all file system events that adds all incoming events into an event loop to handle them outside the
 * event broker's context
 */
open class FileSystemEventLoop : FileSystemObserver {

    /**
     * The channel all events are sent into
     */
    val eventChannel = Channel<FileSystemEvent>()

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