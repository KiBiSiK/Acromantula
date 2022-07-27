 package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.cydhra.acromantula.proto.Bus
import net.cydhra.acromantula.proto.BusServiceGrpcKt

 class BusRpcServer : BusServiceGrpcKt.BusServiceCoroutineImplBase() {

     @ExperimentalCoroutinesApi
     override fun getEventStream(request: Empty): Flow<Bus.Event> {
         // the callback flow internally uses a buffered channel to use store the events generated by the registered
         // listeners until the client consumes them. If the client stops consuming them without closing the flow,
         // the channel will eventually block indefinitely and thus create a resource leak, as more and more
         // coroutines in the EventBroker suspend and never wake up. However, since the coroutines suspend without
         // blocking the thread, the EventBroker should retain functionality.
         return callbackFlow {

         }
    }
}