package net.cydhra.acromantula.workspace.database.mapping

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

/**
 * A table of registered content mapping reference types. It exists per workspace so that plugins may register new
 * content mapping reference types which then get the same numerical id within the database regardless of the order in
 * which plugins are activated (and if plugins are deactivated, the reference type id does not get reused for another
 * plugin)
 */
internal object ContentMappingReferenceTypeTable : IntIdTable() {
    val symbolType = reference("symbol", ContentMappingSymbolTypeTable)
    val identifier = varchar("type", 255)

    init {
        uniqueIndex(symbolType, identifier)
    }
}

internal class ContentMappingReferenceType(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ContentMappingReferenceType>(ContentMappingReferenceTypeTable)

    /**
     * The symbol type this reference type is referring to.
     */
    var symbolType by ContentMappingSymbolType referencedOn ContentMappingReferenceTypeTable.symbolType

    /**
     * A string identifier that represents this reference type within the database (uniquely among other reference
     * types for the same symbol type).
     */
    var uniqueIdentifier by ContentMappingReferenceTypeTable.identifier
}