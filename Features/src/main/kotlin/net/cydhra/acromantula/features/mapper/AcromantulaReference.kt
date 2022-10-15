package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.filesystem.FileEntity

interface AcromantulaReference {

    /**
     * A string representation for this type of reference (independent of the actual data). Used for API interaction.
     */
    val referenceType: String

    /**
     * The symbol this reference points to.
     */
    val referencedSymbol: AcromantulaSymbol

    /**
     * Source file of this reference
     */
    val sourceFile: FileEntity

    /**
     * Called when the [referencedSymbol]'s name is updated.
     *
     * @param newName the new name for the symbol.
     */
    fun onUpdateSymbolName(newName: String)

    /**
     * Generate a string representation of this reference for user interfaces
     */
    fun displayString(): String
}