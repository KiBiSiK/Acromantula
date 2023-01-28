package net.cydhra.acromantula.workspace.util

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.cydhra.acromantula.workspace.filesystem.FileSystemEventBroker
import net.cydhra.acromantula.workspace.filesystem.WorkspaceFileSystem
import org.apache.logging.log4j.LogManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Generic event broker for events of type [E]. Event handlers are called sequentially with events in order they are
 * fired at the broker. Events are handled in a thread separate from the calling thread.
 */
abstract class EventBroker<E, O> {
    protected val registeredObservers = mutableListOf<O>()

    /**
     * Force dispatch of events in sequential order, so parent file events are dispatched first
     */
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    private val sequentialDispatcher = singleThreadExecutor.asCoroutineDispatcher()

    open fun initialize() {

    }

    open fun shutdown() {
        LogManager.getLogger().info("waiting for event dispatch to complete...")
        singleThreadExecutor.shutdown()
        singleThreadExecutor.awaitTermination(5L, TimeUnit.MINUTES)
        LogManager.getLogger().info("event dispatch complete.")
    }

    /**
     * Register a new observer at the event broker.
     */
    open fun registerObserver(observer: O) {
        registeredObservers.add(observer)
    }

    /**
     * Unregister an observer at the event broker
     */
    open fun unregisterObserver(observer: O) {
        registeredObservers.remove(observer)
    }

    /**
     * Migrate all currently registered observers to a new [FileSystemEventBroker]. This method is useful when a new
     * workspace is loaded and thus a new [WorkspaceFileSystem] instance is created. Make sure that database-specific
     * observers are no longer registered
     */
    open fun migrateObservers(newBroker: EventBroker<E, O>) {
        this.registeredObservers.forEach(newBroker::registerObserver)
    }

    /**
     * Dispatch event to all observers
     */
    // we need to fire and forget here, otherwise event handling will delay the caller
    @OptIn(DelicateCoroutinesApi::class)
    fun dispatch(event: E) {
        GlobalScope.launch(sequentialDispatcher) {
            dispatchEvent(event)
        }
    }

    protected abstract suspend fun dispatchEvent(event: E)
}