package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.filesystem.FileEntity

interface AcromantulaReference {

    /**
     * Get the referenced symbol
     */
    fun getReferencedSymbol(): AcromantulaSymbol

    /**
     * Location of the reference within its file
     */
    fun getLocation(): String

    /**
     * The file this reference stems from
     */
    fun getFile(): FileEntity

    /**
     * An implementation may chose to assign [AcromantulaSymbol]s as owners of references to further specify the
     * exact location of a reference within a file. If so, this returns the assigned owner symbol of this reference.
     * If not, this returns null.
     */
    fun getOwner(): AcromantulaSymbol?
}