package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.exporter.ExporterFeature

/**
 * Command to list all available exporter strategies.
 */
class ListExportersCommandInterpreter() : WorkspaceCommandInterpreter<List<String>> {
    override fun evaluate(): List<String> {
        return ExporterFeature.getExporters().toList()
    }
}

