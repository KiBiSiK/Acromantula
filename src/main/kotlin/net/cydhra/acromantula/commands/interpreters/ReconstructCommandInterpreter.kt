package net.cydhra.acromantula.commands.interpreters

import kotlinx.coroutines.CompletableJob
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.features.view.GenerateViewFeature
import net.cydhra.acromantula.workspace.WorkspaceService
import org.apache.logging.log4j.LogManager

/**
 * Reconstruct file content from a representation file in binary form
 *
 * @param fileEntityId optional. the entity id of the file to reconstruct
 * @param viewType which view type to use for the reconstruction
 * @param dataBuffer binary data of a representation file that is to be converted into a workspace file
 */
class ReconstructCommandInterpreter(
    val fileEntityId: Int,
    val viewType: String,
    val dataBuffer: ByteArray
) : WorkspaceCommandInterpreter<Unit> {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override suspend fun evaluate(supervisor: CompletableJob) {
        val file = WorkspaceService.queryPath(fileEntityId)
        GenerateViewFeature.reconstructFromView(file, viewType, dataBuffer)
    }
}

