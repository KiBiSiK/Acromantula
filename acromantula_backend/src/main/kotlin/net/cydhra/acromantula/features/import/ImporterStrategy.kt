package net.cydhra.acromantula.features.import

import net.cydhra.acromantula.data.DirectoryEntity

/**
 * Implementations import different files into the workspace using different strategies. Using [handles] the
 * strategies decide which implementation imports wich file. If two strategies return true on that call, the first
 * one in the list handles the file, so it should be avoided that multiple importers exist for the same file.
 */
interface ImporterStrategy {

    fun handles(fileName: String, fileContent: ByteArray): Boolean

    fun import(parent: DirectoryEntity, fileName: String, fileContent: ByteArray)
}