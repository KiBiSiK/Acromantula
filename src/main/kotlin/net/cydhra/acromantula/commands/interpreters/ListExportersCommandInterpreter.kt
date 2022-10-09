package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.exporter.ExporterFeature

/**
 * Command to list all available exporter strategies.
 */
class ListExportersCommandInterpreter() : WorkspaceCommandInterpreter<List<String>> {
    override val synchronous: Boolean = true

    override suspend fun evaluate(): List<String> {
        return ExporterFeature.getExporters().map { it.name }.toList()
    }
}

