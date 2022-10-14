package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.filesystem.FileEntity

interface AcromantulaSymbol {

    /**
     * Whether this symbol supports renaming
     */
    val canBeRenamed: Boolean

    /**
     * Where this symbol is located, or null if this is an inferred external symbol
     */
    val sourceFile: FileEntity?

    /**
     * The actual symbol content that is referenced by the data structures of the mapped file. This does not need to
     * be unique. Use [updateName] to update the name and all references if [canBeRenamed] is true.
     */
    fun getName(): String

    /**
     * Rename the symbol and update all references iff [canBeRenamed] is true. The implementation of this interface
     * is responsible for calling [AcromantulaReference.onUpdateSymbolName] on all references.
     */
    suspend fun updateName(newName: String)

    /**
     * Generate a string representation of this symbol for user interfaces
     */
    fun displayString(): String
}