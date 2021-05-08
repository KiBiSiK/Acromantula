package net.cydhra.acromantula.workspace.database.mapping

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

/**
 * A table of registered content mapping symbol types. It exists per workspace so that plugins may register new
 * content mapping symbol types which then get the same numerical id within the database regardless of the order in
 * which plugins are activated (and if plugins are deactivated, the symbol type id does not get reused for another
 * plugin)
 */
internal object ContentMappingSymbolTypeTable : org.jetbrains.exposed.dao.id.IntIdTable() {
    val identifier = varchar("type", 255).uniqueIndex()
}

internal class ContentMappingSymbolType(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ContentMappingSymbolType>(ContentMappingSymbolTypeTable)

    /**
     * A string identifier that uniquely represents this symbol type within the database.
     */
    var uniqueIdentifier by ContentMappingSymbolTypeTable.identifier
}
