package net.cydhra.acromantula.commands.interpreters

import kotlinx.coroutines.CompletableJob
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.view.GenerateViewFeature

/**
 * Command to list all available exporter strategies.
 */
class ListViewTypesCommandInterpreter() : WorkspaceCommandInterpreter<List<Pair<String, String>>> {
    override suspend fun evaluate(supervisor: CompletableJob): List<Pair<String, String>> {
        return GenerateViewFeature.getViewTypes()
    }
}

