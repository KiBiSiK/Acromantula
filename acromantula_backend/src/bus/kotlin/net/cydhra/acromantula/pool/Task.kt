package net.cydhra.acromantula.pool

import com.google.common.util.concurrent.ListenableFuture
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.pool.event.TaskStatusChangedEvent

/**
 * A potentially long-running asynchronous task that has been registered and scheduled in the [WorkerPool]. It is
 * identifiable by a unique id. Clients can request status information about the task at any time.
 */
class Task(val id: Int, private val future: ListenableFuture<Unit>, initialStatus: String) {
    var status: String = initialStatus
        set(value) {
            synchronized(status) {
                field = value
                EventBroker.fireEvent(TaskStatusChangedEvent(this.id, value))
            }
        }
}