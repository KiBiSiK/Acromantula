package net.cydhra.acromantula.rpc.service

import io.grpc.stub.StreamObserver
import net.cydhra.acromantula.proto.*

class ViewRpcServer : ViewServiceGrpc.ViewServiceImplBase() {
    override fun view(request: ViewCommand, responseObserver: StreamObserver<ViewResponse>) {
        TODO("not yet implemented")
    }

    override fun exportView(request: ExportViewCommand, responseObserver: StreamObserver<ExportViewResponse>) {
        TODO("not yet implemented")
    }
}