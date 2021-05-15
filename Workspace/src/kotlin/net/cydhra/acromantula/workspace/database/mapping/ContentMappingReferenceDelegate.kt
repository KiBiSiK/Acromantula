package net.cydhra.acromantula.workspace.database.mapping

/**
 * A wrapper for registered reference types that can be used by higher level functionality to access content mappings
 * within the database. The actual database element is not exposed, as the delegate remains valid even when the
 * database connection is closed and another one is opened.
 *
 * @param uniqueIdentifier the string identifier that uniquely identifies this reference type among all reference
 * types for a specified symbol type
 * @param symbolType the symbol type references of this type refer to
 * @param referenceType the internal [ContentMappingReferenceType] that is used within the database. It may be
 * transparently exchanged with another database entity later, so it must not be exposed to higher level modules.
 */
class ContentMappingReferenceDelegate internal constructor(
    val uniqueIdentifier: String,
    val symbolType: ContentMappingSymbolTypeDelegate,
    internal var referenceType: ContentMappingReferenceType
)