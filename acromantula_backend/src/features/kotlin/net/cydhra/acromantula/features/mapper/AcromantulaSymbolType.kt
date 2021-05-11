package net.cydhra.acromantula.features.mapper

/**
 * A model symbol type. Any file entity may define any number of symbols (for example function names
 * in code files) that can be referenced by other files.
 *
 * @param typeName A unique identifier for this type of symbol
 * @see doesSupportRenaming whether this symbol supports renaming
 */
abstract class AcromantulaSymbolType(typeName: String, val doesSupportRenaming: Boolean) {

    /**
     * A unique identifier for this type of reference
     */
    val symbolType = typeName

    /**
     * Called when the name of this symbol is changed by the user. Implementors must update the symbol name in the
     * source file. References of this symbol must not be updated, as their [AcromantulaReferenceType] implementation
     * is called separately
     */
    abstract fun onUpdateName(newName: String)
}