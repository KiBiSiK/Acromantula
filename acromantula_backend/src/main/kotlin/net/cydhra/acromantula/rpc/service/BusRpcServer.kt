package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import kotlinx.coroutines.flow.Flow
import net.cydhra.acromantula.proto.Bus
import net.cydhra.acromantula.proto.BusServiceGrpcKt

class BusRpcServer : BusServiceGrpcKt.BusServiceCoroutineImplBase() {
    override fun getEventStream(request: Empty): Flow<Bus.Event> {
        return super.getEventStream(request)
    }
}