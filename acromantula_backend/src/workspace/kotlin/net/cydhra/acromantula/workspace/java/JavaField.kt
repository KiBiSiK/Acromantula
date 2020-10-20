package net.cydhra.acromantula.workspace.java

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

internal object JavaFieldTable : IntIdTable("JavaFields") {
    val identifier = reference("identifier", JavaIdentifierTable)
    val name = varchar("name", MAX_IDENTIFIER_LENGTH)
    val access = integer("access")
    val owner = reference("owner", JavaClassTable)
}

class JavaField(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<JavaField>(JavaFieldTable)

    var identifier by JavaIdentifier referencedOn JavaFieldTable.identifier
        internal set
    var name by JavaFieldTable.name
        internal set
    var access by JavaFieldTable.access
        internal set
    var owner by JavaClass referencedOn JavaFieldTable.owner
        internal set
}
