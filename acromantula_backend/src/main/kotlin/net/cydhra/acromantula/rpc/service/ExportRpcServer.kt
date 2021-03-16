package net.cydhra.acromantula.rpc.service

import com.google.protobuf.Empty
import net.cydhra.acromantula.commands.CommandDispatcherService
import net.cydhra.acromantula.commands.interpreters.ExportCommandInterpreter
import net.cydhra.acromantula.features.exporter.ExporterFeature
import net.cydhra.acromantula.proto.*

class ExportRpcServer : ExportServiceGrpcKt.ExportServiceCoroutineImplBase() {
    override suspend fun exportFile(request: ExportCommand): Empty {
        val result = CommandDispatcherService.dispatchCommand(
            when {
                request.fileId != -1 -> ExportCommandInterpreter(request.fileId, request.exporter, request.targetPath)
                request.filePath != null -> ExportCommandInterpreter(
                    request.filePath,
                    request.exporter,
                    request.targetPath
                )
                else -> throw IllegalArgumentException("either fileId or filePath must be defined")
            }
        ).await()

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
                            this.name = exporter
                        }
                    }
                    .toList()
        }
    }
}