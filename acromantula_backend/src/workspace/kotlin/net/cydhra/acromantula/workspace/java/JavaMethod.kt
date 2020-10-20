package net.cydhra.acromantula.workspace.java

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

internal object JavaMethodTable : IntIdTable("JavaMethods") {
    val identifier = reference("identifier", JavaIdentifierTable)
    val owner = reference("owner", JavaClassTable)
    val name = varchar("name", MAX_IDENTIFIER_LENGTH)
}

class JavaMethod(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<JavaMethod>(JavaMethodTable)

    var identifier by JavaIdentifier referencedOn JavaMethodTable.identifier
        internal set
    var owner by JavaClass referencedOn JavaMethodTable.owner
        internal set
    var name by JavaMethodTable.name
        internal set
}
