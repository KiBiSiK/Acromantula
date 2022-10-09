package net.cydhra.acromantula.features.exporter

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.OutputStream

object ExporterFeature {

    private val logger = LogManager.getLogger()

    private val registeredExporterStrategies = mutableMapOf<String, ExporterStrategy>()

    init {
        registerExporterStrategy(GenericExporterStrategy())
        registerExporterStrategy(ZipExporterStrategy())
    }

    /**
     * Export a given file at a given target path. This is a convenience method that automatically creates the file.
     */
    fun exportFile(fileEntity: FileEntity, exporterStrategyName: String, targetFileName: String) {
        val targetFile = File(targetFileName)
        targetFile.createNewFile()
        targetFile.outputStream().use { stream ->
            exportFile(fileEntity, exporterStrategyName, stream)
        }
    }

    /**
     * Export a given file into a given output stream using a strategy identified by its name. The output stream is
     * not closed by the method.
     */
    fun exportFile(fileEntity: FileEntity, exporterStrategyName: String, outputStream: OutputStream) {
        val strategy = registeredExporterStrategies[exporterStrategyName]
            ?: throw IllegalArgumentException("exporter strategy \"$exporterStrategyName\" is unknown")

        strategy.exportFile(fileEntity, outputStream)
    }

    /**
     * Register an exporter strategy at the feature. Its name must be unique
     */
    fun registerExporterStrategy(exporterStrategy: ExporterStrategy) {
        this.registeredExporterStrategies[exporterStrategy.name] = exporterStrategy
    }

    /**
     * Get a list of names for all available exporters
     */
    fun getExporters(): Collection<ExporterStrategy> {
        return this.registeredExporterStrategies.values
    }
}