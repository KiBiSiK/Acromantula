package net.cydhra.acromantula.rpc.service

import io.grpc.stub.StreamObserver
import net.cydhra.acromantula.proto.ListFilesCommand
import net.cydhra.acromantula.proto.ListFilesResponse
import net.cydhra.acromantula.proto.WorkspaceServiceGrpc

class WorkspaceRpcServer : WorkspaceServiceGrpc.WorkspaceServiceImplBase() {
    override fun listFiles(request: ListFilesCommand, responseObserver: StreamObserver<ListFilesResponse>) {
        TODO("not yet implemented")
    }
}