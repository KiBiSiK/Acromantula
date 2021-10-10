package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.view.GenerateViewFeature

/**
 * Command to list all available exporter strategies.
 */
class ListViewTypesCommandInterpreter() : WorkspaceCommandInterpreter<List<Pair<String, String>>> {
    override val synchronous: Boolean = true

    override suspend fun evaluate(): List<Pair<String, String>> {
        return GenerateViewFeature.getViewTypes()
    }
}

