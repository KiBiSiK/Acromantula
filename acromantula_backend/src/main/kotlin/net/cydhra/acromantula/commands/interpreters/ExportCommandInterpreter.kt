package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.exporter.ExporterFeature
import net.cydhra.acromantula.workspace.WorkspaceService

/**
 * Command to export files from workspace.
 *
 * @param fileEntityId optional. the entity id of parent directory
 * @param filePath optional. the path of the directory in workspace
 * @param exporterName name of the exporter strategy to use
 * @param targetFileName path of target file
 */
class ExportCommandInterpreter private constructor(
    val fileEntityId: Int? = null,
    val filePath: String? = null,
    val exporterName: String,
    val targetFileName: String
) : WorkspaceCommandInterpreter<Unit> {

    /**
     * Command to import files into workspace.
     *
     * @param fileEntityId optional. the entity id of parent directory
     *  @param exporterName name of the exporter strategy to use
     * @param targetFileName name of target file
     */
    constructor(fileEntityId: Int? = null, exporterName: String, targetFileName: String) : this(
        fileEntityId, null, exporterName,
        targetFileName
    )

    /**
     * Command to import files into workspace.
     *
     * @param filePath optional. the path of the directory in workspace
     * @param exporterName name of the exporter strategy to use
     * @param targetFileName name of target file
     */
    constructor(filePath: String? = null, exporterName: String, targetFileName: String) : this(
        null, filePath,
        exporterName,
        targetFileName
    )

    override fun evaluate() {
        val file = when {
            fileEntityId != null -> WorkspaceService.queryPath(fileEntityId)
            filePath != null -> WorkspaceService.queryPath(filePath)
            else -> throw IllegalStateException("either the entity id or the path of the file must be present")
        }

        ExporterFeature.exportFile(file, exporterName, targetFileName)
    }
}

