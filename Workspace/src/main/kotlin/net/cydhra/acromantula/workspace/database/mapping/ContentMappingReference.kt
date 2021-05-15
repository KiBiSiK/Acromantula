package net.cydhra.acromantula.workspace.database.mapping

import net.cydhra.acromantula.workspace.database.DatabaseManager
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import net.cydhra.acromantula.workspace.filesystem.FileTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

object ContentMappingReferenceTable : org.jetbrains.exposed.dao.id.IntIdTable() {
    val type = reference("type", ContentMappingReferenceTypeTable)

    // the referenced symbol
    val symbol = reference("symbol", ContentMappingSymbolTable)

    val file = reference("file", FileTable)

    // an optional symbol that is the hierarchical owner of the reference. If this does not apply, it is null
    val owner = reference("owner", ContentMappingSymbolTable).nullable()

    // an unstructured hint about the precise location of the reference, so it can be used by tools
    val location = varchar("location", Short.MAX_VALUE - 1).nullable()
}

class ContentMappingReference(entityId: EntityID<Int>) : IntEntity(entityId) {

    companion object : IntEntityClass<ContentMappingReference>(ContentMappingReferenceTable)

    private var typeEntity by ContentMappingReferenceType referencedOn ContentMappingReferenceTable.type

    /**
     * The symbol type
     */
    var type: ContentMappingReferenceDelegate
        get() {
            return DatabaseManager.getReferenceTypeDelegate(this.typeEntity)
        }
        set(value) {
            this.typeEntity = value.referenceType
        }

    /**
     * The referenced symbol
     */
    var symbol by ContentMappingSymbol referencedOn ContentMappingReferenceTable.symbol

    /**
     * The file that contains this reference
     */
    var file by FileEntity referencedOn ContentMappingReferenceTable.file

    /**
     * Optional symbol that can be seen as the parent of this reference (e.g. a method as the onwer of another method
     * call)
     */
    var owner by ContentMappingSymbol optionalReferencedOn ContentMappingReferenceTable.owner

    /**
     * An unstructured string that can be utilized by the plugin providing [type] to store the location of the
     * reference and tell the GUI where to find it.
     */
    var location by ContentMappingReferenceTable.location
}