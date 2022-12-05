package net.cydhra.acromantula.workspace.filesystem

/**
 * Subject for observable file system events. The events occur in [WorkspaceFileSystem], but for reasons of code
 * structure, all observer-related logic is aggregated in this helper class. Each [WorkspaceFileSystem] object
 * requires exactly one [FileSystemEventBroker] instance and vise versa. Each file-system related event is relayed to the
 * [FileSystemEventBroker] instance and then observers are notified from here. One observer that is automatically
 * attached by the [WorkspaceFileSystem] is the database handler (TODO: reference that class here once it is
 * implemented) which will mirror all events into the database.
 */
internal class FileSystemEventBroker {

    private val registeredObservers = mutableListOf<FileSystemObserver>()

    /**
     * Register a new observer at the event broker.
     */
    internal fun registerObserver(observer: FileSystemObserver) {
        registeredObservers.add(observer)
    }

    /**
     * Unregister an observer at the event broker
     */
    internal fun unregisterObserver(observer: FileSystemObserver) {
        registeredObservers.remove(observer)
    }

    /**
     * Migrate all currently registered observers to a new [FileSystemEventBroker]. This method is useful when a new
     * workspace is loaded and thus a new [WorkspaceFileSystem] instance is created. Make sure that database-specific
     * observers are no longer registered
     */
    internal fun migrateObservers(newBroker: FileSystemEventBroker) {
        this.registeredObservers.forEach(newBroker::registerObserver)
    }

}