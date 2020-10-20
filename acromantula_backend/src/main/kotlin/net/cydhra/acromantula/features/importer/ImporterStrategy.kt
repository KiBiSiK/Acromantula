package net.cydhra.acromantula.features.importer

import net.cydhra.acromantula.workspace.filesystem.DirectoryEntity
import java.io.PushbackInputStream

/**
 * Implementations import different files into the workspace using different strategies. Using [handles] the
 * strategies decide which implementation imports wich file. If two strategies return true on that call, the first
 * one in the list handles the file, so it should be avoided that multiple importers exist for the same file.
 */
interface ImporterStrategy {

    fun handles(fileName: String, fileContent: PushbackInputStream): Boolean

    fun import(parent: DirectoryEntity, fileName: String, fileContent: PushbackInputStream)
}