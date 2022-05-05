package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.mapper.MapperFeature

/**
 * Command to rename symbols in the workspace.
 *
 * @param symbolType type of the symbol to remap
 * @param symbolIdentifier current name of the symbol
 * @param newIdentifier new name for the symbol
 */
class RenameCommandInterpreter constructor(
    private val symbolType: String,
    private val symbolIdentifier: String,
    private val newIdentifier: String
) : WorkspaceCommandInterpreter<Unit> {

    override suspend fun evaluate() {
        MapperFeature.remapSymbol(symbolType, symbolIdentifier, newIdentifier)
    }
}

