package net.cydhra.acromantula.features.mapper

import net.cydhra.acromantula.workspace.database.mapping.ContentMappingReference
import net.cydhra.acromantula.workspace.database.mapping.ContentMappingSymbol

/**
 * A model reference type. Any file entity may define any number of symbols (for example function names
 * in code files) that can be referenced by other files. Any type of symbol may have one or more types of references
 * pointing towards them. This class models the behaviour of one type of reference and provides strategies of
 * modifying and interacting with them.
 *
 * @param typeName A unique identifier for this type of reference
 *
 * @see AcromantulaSymbolType
 */
abstract class AcromantulaReferenceType(typeName: String) {

    /**
     * A unique identifier for this type of reference
     */
    val referenceType = typeName

    /**
     * Called before a symbol is renamed so the references to the symbol in affected files can be updated.
     *
     * @param symbol the symbol that is being renamed
     * @param reference the reference that is to be updated
     * @param newName the new symbol name
     */
    abstract fun onUpdateSymbolName(symbol: ContentMappingSymbol, reference: ContentMappingReference, newName: String)
}