package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * @param type A unique string identifier for this reference type. This must never change for instances and must be
 * the same for symbols that are indistinguishable in their functionality
 */
abstract class AcromantulaReference(val type: String) {

    /**
     * The database reference handle
     */
    internal lateinit var reference: ContentMappingReference

    /**
     * Get the referenced symbol
     */
    abstract fun getReferencedSymbol(): AcromantulaSymbol

    /**
     * Location of the reference within its file
     */
    abstract fun getLocation(): String

    /**
     * The file this reference stems from
     */
    abstract fun getFile(): FileEntity

    /**
     * An implementation may chose to assign [AcromantulaSymbol]s as owners of references to further specify the
     * exact location of a reference within a file. If so, this returns the assigned owner symbol of this reference.
     * If not, this returns null.
     */
    abstract fun getOwner(): AcromantulaSymbol?
}