package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ExportCommandInterpreter
import net.cydhra.acromantula.features.exporter.ExporterFeature
import net.cydhra.acromantula.proto.*

class ExportRpcServer : ExportServiceGrpcKt.ExportServiceCoroutineImplBase() {
    override suspend fun exportFile(request: ExportCommand): Empty {
        val interpreter = when (request.fileIdCase) {
            ExportCommand.FileIdCase.ID ->
                ExportCommandInterpreter(request.id, request.exporter.name, request.targetPath)

            ExportCommand.FileIdCase.FILEPATH ->
                ExportCommandInterpreter(
                    request.filePath,
                    request.exporter.name,
                    request.targetPath
                )

            null, ExportCommand.FileIdCase.FILEID_NOT_SET -> throw MissingTargetFileException()
        }

        val result = CommandDispatcherService.dispatchCommand("[RPC] export ${request.targetPath}", interpreter).await()

        result.onFailure {
            throw it
        }

        return Empty.getDefaultInstance()
    }

    override suspend fun getExporters(request: Empty): ExportersList {
        return exportersList {
            this.exporters =
                ExporterFeature.getExporters()
                    .map { exporter ->
                        exporter {
                            this.name = exporter.name
                            this.defaultExtension = exporter.defaultFileExtension
                            this.supportedArchives = exporter.supportedArchiveTypes.toList()
                        }
                    }
                    .toList()
        }
    }
}