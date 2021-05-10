package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * A symbol and functionality to work with it
 *
 * @param type A unique string identifier for this symbol type. This must never change for instances and must be the
 * same for symbols that are indistinguishable in their functionality
 */
abstract class AcromantulaSymbol(val type: String) {

    /**
     * The database symbol handle
     */
    internal lateinit var symbol: ContentMappingSymbol

    /**
     * Get the symbol identifier. It should be unique among all symbols of this type (though this is not enforced)
     */
    abstract fun getIdentifier(): String

    /**
     * Name of the symbol. This is what can be changed with the remapper
     */
    abstract fun getName(): String

    /**
     * Location of the symbol within its file
     */
    abstract fun getLocation(): String

    /**
     * The file this symbol stems from
     */
    abstract fun getFile(): FileEntity

    /**
     * Get a list of all references on this symbol
     */
    abstract fun findReferences(): List<AcromantulaReference>

    /**
     * Change the name of this symbol.
     *
     * @return true, if the symbol was renamed, false if the new name is illegal for this symbol
     */
    abstract fun rename(newName: String): Boolean
}