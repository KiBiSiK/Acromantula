package net.cydhra.acromantula.rpc.service

import io.grpc.stub.StreamObserver
import net.cydhra.acromantula.proto.ImportCommand
import net.cydhra.acromantula.proto.ImportResponse
import net.cydhra.acromantula.proto.ImportServiceGrpc

class ImportRpcServer : ImportServiceGrpc.ImportServiceImplBase() {

    override fun importFile(request: ImportCommand, responseObserver: StreamObserver<ImportResponse>) {
        val response = ImportResponse.newBuilder().build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}