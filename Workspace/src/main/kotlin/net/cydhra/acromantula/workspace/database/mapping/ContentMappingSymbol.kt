package net.cydhra.acromantula.workspace.database.mapping

import net.cydhra.acromantula.workspace.database.DatabaseMappingsManager
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object ContentMappingSymbolTable : IdTable<String>() {
    override val id: Column<EntityID<String>> = varchar("identifier", Short.MAX_VALUE - 1).entityId()
    val type = reference("type", ContentMappingSymbolTypeTable)
    val name = varchar("name", Short.MAX_VALUE - 1)
    val file = reference("file", FileTable).nullable()
    val location = varchar("location", Short.MAX_VALUE - 1).nullable()
}

class ContentMappingSymbol(entityId: EntityID<String>) : Entity<String>(entityId) {
    companion object : EntityClass<String, ContentMappingSymbol>(ContentMappingSymbolTable)

    private var databaseType by ContentMappingSymbolType referencedOn ContentMappingSymbolTable.type

    /**
     * The registered [ContentMappingSymbolTypeDelegate] of this reference
     */
    var type: ContentMappingSymbolTypeDelegate
        get() {
            return DatabaseMappingsManager.getSymbolDelegate(this.databaseType)
        }
        internal set(value) {
            databaseType = value.symbolType
        }

    /**
     * The file that this symbol is contained within
     */
    var file by FileEntity optionalReferencedOn ContentMappingSymbolTable.file

    /**
     * (Preferably) unique symbol identifier
     */
    var identifier by ContentMappingSymbolTable.id
        internal set

    /**
     * Name of the symbol that can be changed by the mapper.
     */
    var name by ContentMappingSymbolTable.name
        internal set

    /**
     * An unstructured hint that can be used by the plugin providing this [type] to hint itself and the GUI where to
     * find this symbol in the owner file.
     */
    var location by ContentMappingSymbolTable.location
}