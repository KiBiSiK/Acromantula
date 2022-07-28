package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter

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
    override val synchronous: Boolean = true

    override suspend fun evaluate(): List<Pair<Int, String>> {
//        return MapperFeature.getReferencesRepresentation(type, symbol)
        TODO("implement symbol reference navigation")
    }
}

