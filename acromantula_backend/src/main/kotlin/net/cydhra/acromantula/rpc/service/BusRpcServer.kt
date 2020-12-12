package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.events.ApplicationShutdownEvent
import net.cydhra.acromantula.bus.events.ApplicationStartupEvent
import net.cydhra.acromantula.pool.event.TaskFinishedEvent
import net.cydhra.acromantula.pool.event.TaskStatusChangedEvent
import net.cydhra.acromantula.proto.*

class BusRpcServer : BusServiceGrpcKt.BusServiceCoroutineImplBase() {

    @ExperimentalCoroutinesApi
    override fun getEventStream(request: Empty): Flow<Bus.Event> {
        return callbackFlow {
            val applicationStartupListener: suspend (ApplicationStartupEvent) -> Unit = {
                offer(bus_Event {
                    this.startupEvent = bus_ApplicationStartUpEvent {}
                })
            }

            val applicationShutdownListener: suspend (ApplicationShutdownEvent) -> Unit = {
                offer(bus_Event {
                    this.shutdownEvent = bus_ApplicationShutdownEvent {}
                })
            }

            val taskFinishedListener: suspend (TaskFinishedEvent) -> Unit = { event ->
                offer(bus_Event {
                    this.taskFinishedEvent = bus_TaskFinishedEvent {
                        taskId = event.taskId
                    }
                })
            }

            val taskStatusChangedListener: suspend (TaskStatusChangedEvent) -> Unit = { event ->
                bus_Event {
                    this.taskStatusChangedEvent = bus_TaskStatusChangedEvent {
                        taskId = event.taskId
                        newStatus = event.newStatus
                    }
                }
            }

            EventBroker.registerEventListener(ApplicationStartupEvent::class, applicationStartupListener)
            EventBroker.registerEventListener(ApplicationShutdownEvent::class, applicationShutdownListener)
            EventBroker.registerEventListener(TaskFinishedEvent::class, taskFinishedListener)
            EventBroker.registerEventListener(TaskStatusChangedEvent::class, taskStatusChangedListener)

            invokeOnClose {
                // TODO unregister listeners
            }
        }
    }
}