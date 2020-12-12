package net.cydhra.acromantula.pool

import com.google.common.util.concurrent.ListenableFuture
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.pool.event.TaskStatusChangedEvent
import java.util.*

/**
 * A potentially long-running asynchronous task that has been registered and scheduled in the [WorkerPool]. It is
 * identifiable by a unique id. Clients can request status information about the task at any time.
 */
class Task<V>(val id: Int, private val future: ListenableFuture<V>, initialStatus: String) {
    var status: String = initialStatus
        set(value) {
            synchronized(status) {
                field = value
                EventBroker.fireEvent(TaskStatusChangedEvent(this.id, value))
            }
        }

    var finished: Boolean = false
        internal set

    /**
     * The result of the task if ready, or an exception if the task failed
     */
    var result = Optional.empty<Result<V>>()
        internal set

    override fun toString(): String {
        return "Task $id[finished:$finished; status:\"$status\"]"
    }
}