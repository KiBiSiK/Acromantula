package net.cydhra.acromantula.features.view

import net.cydhra.acromantula.workspace.disassembly.FileView
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
     * Whether this view generator supports regeneration of the original file from representation data. This can be
     * used to edit the file using the representation as a surrogate, easing the editing process.
     */
    val supportsReconstruction: Boolean
        get() = false

    /**
     * Returns true, if this view generator is able to generate a view from the given file. False, if this strategy
     * cannot be used with this file
     */
    fun handles(fileEntity: FileEntity): Boolean

    /**
     * Generate a view from the given file entity and return a handle to the generated view file.
     */
    fun generateView(fileEntity: FileEntity): FileView

    /**
     * Use the data from a view representation to reconstruct the file from it. The data must be of the [fileType]
     * defined by this implementation. This method must not be called if [supportsReconstruction] is false, otherwise
     * an [UnsupportedOperationException] is thrown.
     */
    fun reconstructFromView(fileEntity: FileEntity, buffer: ByteArray) {
        throw UnsupportedOperationException("Reconstruction of file is unsupported for this view type")
    }
}