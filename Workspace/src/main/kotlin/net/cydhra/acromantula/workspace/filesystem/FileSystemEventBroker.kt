package net.cydhra.acromantula.workspace.filesystem

import kotlinx.coroutines.*
import net.cydhra.acromantula.workspace.util.EventBroker

/**
 * Subject for observable file system events. The events occur in [WorkspaceFileSystem], but for reasons of code
 * structure, all observer-related logic is aggregated in this helper class. Each [WorkspaceFileSystem] object
 * requires exactly one [FileSystemEventBroker] instance and vise versa. Each file-system related event is relayed to the
 * [FileSystemEventBroker] instance and then observers are notified from here. One observer that is automatically
 * attached by the [WorkspaceFileSystem] is the database handler [FileSystemDatabaseSync]
 * which will mirror all events into the database.
 */
internal class FileSystemEventBroker : EventBroker<FileSystemEvent, FileSystemObserver>() {

    override suspend fun dispatchEvent(event: FileSystemEvent) {
        @Suppress("REDUNDANT_ELSE_IN_WHEN") // see else branch
        when (event) {
            is FileSystemEvent.ArchiveCreatedEvent -> registeredObservers.forEach {
                it.onArchiveCreated(
                    event
                )
            }

            is FileSystemEvent.FileCreatedEvent -> registeredObservers.forEach { it.onFileCreated(event) }
            is FileSystemEvent.FileMovedEvent -> registeredObservers.forEach { it.onFileMoved(event) }
            is FileSystemEvent.FileRenamedEvent -> registeredObservers.forEach { it.onFileRenamed(event) }
            is FileSystemEvent.FileUpdatedEvent -> registeredObservers.forEach { it.onFileUpdated(event) }
            is FileSystemEvent.FileDeletedEvent -> registeredObservers.forEach { it.onFileDeleted(event) }
            is FileSystemEvent.ViewCreatedEvent -> registeredObservers.forEach { it.onViewCreated(event) }
            is FileSystemEvent.ViewDeletedEvent -> registeredObservers.forEach { it.onViewDeleted(event) }
            else -> {
                // this should be impossible to reach, however because this when-expression does not
                // return a value, it is not considered an error if this when is not exhaustive because
                // an event was added to the sealed class FileSystemEvent. This assertion error exists
                // to make sure we notice this mistake should it happen in the future
                throw AssertionError("unknown event dispatched")
            }
        }
    }
}