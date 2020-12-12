package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.cydhra.acromantula.proto.Bus
import net.cydhra.acromantula.proto.BusServiceGrpc

class BusRpcServer : BusServiceGrpc.BusServiceImplBase() {
    override fun getEventStream(request: Empty, responseObserver: StreamObserver<Bus.Event>) {
        TODO("not implemented yet")
    }
}