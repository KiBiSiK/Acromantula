package net.cydhra.acromantula.features.importer

import net.cydhra.acromantula.workspace.filesystem.FileEntity
import java.io.PushbackInputStream

/**
 * Implementations import different files into the workspace using different strategies. Using [handles] the
 * strategies decide which implementation imports wich file. If two strategies return true on that call, the first
 * one in the list handles the file, so it should be avoided that multiple importers exist for the same file.
 */
interface ImporterStrategy {

    suspend fun handles(fileName: String, fileContent: PushbackInputStream): Boolean

    suspend fun import(parent: FileEntity?, fileName: String, fileContent: PushbackInputStream)
}