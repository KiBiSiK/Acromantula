package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.mapper.AcromantulaSymbol
import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.workspace.WorkspaceService

/**
 * Command to list all symbols in a given file.
 *
 * @param fileEntityId optional. the entity id of the file to list symbols from
 * @param filePath optional. the path of the file to list symbols from
 */
class ListSymbolsCommandInterpreter(
    val fileEntityId: Int? = null,
    val filePath: String? = null,
) : WorkspaceCommandInterpreter<List<AcromantulaSymbol>> {
    override val synchronous: Boolean = true

    /**
     * Command to list all symbols in a given file.
     *
     * @param fileEntityId optional. the entity id of the file
     */
    constructor(fileEntityId: Int? = null) : this(fileEntityId, null)

    /**
     * Command to list all symbols in a given file.
     *
     * @param filePath optional. the path of the file
     */
    constructor(filePath: String? = null) : this(null, filePath)

    override suspend fun evaluate(): List<AcromantulaSymbol> {
        val file = when {
            fileEntityId != null -> WorkspaceService.queryPath(fileEntityId)
            filePath != null -> WorkspaceService.queryPath(filePath)
            else -> throw IllegalArgumentException("either file id or file path must be present")
        }

        return MapperFeature.getSymbolsInFile(file)
    }
}

