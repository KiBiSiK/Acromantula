package net.cydhra.acromantula.rpc.service

import io.grpc.stub.StreamObserver
import net.cydhra.acromantula.proto.ExportCommand
import net.cydhra.acromantula.proto.ExportResponse
import net.cydhra.acromantula.proto.ExportServiceGrpc

class ExportRpcServer : ExportServiceGrpc.ExportServiceImplBase() {
    override fun exportFile(request: ExportCommand, responseObserver: StreamObserver<ExportResponse>) {
        TODO("not yet implemented")
    }
}