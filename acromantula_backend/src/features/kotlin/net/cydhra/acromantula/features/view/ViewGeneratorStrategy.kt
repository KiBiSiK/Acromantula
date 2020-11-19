package net.cydhra.acromantula.features.view

import net.cydhra.acromantula.workspace.disassembly.FileRepresentation
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileType

/**
 * A strategy to interpret a certain type of files using a specific parsing method and generate a human readable
 * representation from it. This can be image data, textual representations and similar interpretations of data. A
 * strategy can decide if it will try to handle a file using [handles]. If [handles] returns true for a given file,
 * [generateView] will generate a representation of the file and store in in workspace.
 */
interface ViewGeneratorStrategy {

    /**
     * A unique identifier for this view strategy, so generated views can be reused instead of being freshly generated.
     */
    val viewType: String

    /**
     * The file type that this view generates.
     */
    val fileType: FileType

    /**
     * Returns true, if this view generator is able to generate a view from the given file. False, if this strategy
     * cannot be used with this file
     */
    fun handles(fileEntity: FileEntity): Boolean

    /**
     * Generate a view from the given file entity and return a handle to the generated view file.
     */
    fun generateView(fileEntity: FileEntity): FileRepresentation
}