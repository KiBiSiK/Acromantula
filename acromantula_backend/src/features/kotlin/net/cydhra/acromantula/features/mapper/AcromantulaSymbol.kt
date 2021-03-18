package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * A symbol and functionality to work with it
 */
interface AcromantulaSymbol {

    /**
     * Get the symbol identifier. It should be unique among all symbols of this type (though this is not enforced)
     */
    fun getIdentifier(): String

    /**
     * Name of the symbol. This is what can be changed with the remapper
     */
    fun getName(): String

    /**
     * Location of the symbol within its file
     */
    fun getLocation(): String

    /**
     * The file this symbol stems from
     */
    fun getFile(): FileEntity

    /**
     * Get a list of all references on this symbol
     */
    fun findReferences(): List<AcromantulaReference>

    /**
     * Change the name of this symbol.
     *
     * @return true, if the symbol was renamed, false if the new name is illegal for this symbol
     */
    fun rename(newName: String): Boolean
}