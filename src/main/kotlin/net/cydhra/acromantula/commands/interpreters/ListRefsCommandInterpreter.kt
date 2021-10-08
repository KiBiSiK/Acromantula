package net.cydhra.acromantula.commands.interpreters

import kotlinx.coroutines.CompletableJob
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.mapper.MapperFeature

/**
 * Command to list all references to a given symbol.
 *
 * @param type the symbol type identifier
 * @param symbol string identifier of the symbol
 */
class ListRefsCommandInterpreter(
    private val type: String,
    private val symbol: String
) : WorkspaceCommandInterpreter<List<Pair<Int, String>>> {
    override suspend fun evaluate(supervisor: CompletableJob): List<Pair<Int, String>> {
        return MapperFeature.getReferences(type, symbol)
    }
}

