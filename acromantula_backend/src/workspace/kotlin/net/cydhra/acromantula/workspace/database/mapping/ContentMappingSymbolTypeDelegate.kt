package net.cydhra.acromantula.workspace.database.mapping

/**
 * A wrapper for registered symbol types that can be used by higher level functionality to access content mappings
 * within the database. The actual database element is not exposed, as the delegate remains valid even when the
 * database connection is closed and another one is opened.
 *
 * @param uniqueIdentifier the string identifier that uniquely identifies this symbol type among all symbol types
 * @param symbolType the internal [ContentMappingSymbolType] that is used within the database. It may be
 * transparently exchanged with another database entity later, so it must not be exposed to higher level modules.
 */
class ContentMappingSymbolTypeDelegate internal constructor(
    val uniqueIdentifier: String,
    internal var symbolType: ContentMappingSymbolType
)