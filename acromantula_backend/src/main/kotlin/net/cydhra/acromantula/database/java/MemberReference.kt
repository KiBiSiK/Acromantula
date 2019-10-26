package net.cydhra.acromantula.database.java

import org.jetbrains.exposed.dao.IntIdTable

class MemberReference

object MemberReferenceTable : IntIdTable("references") {
    val referrer = reference("referrer", JavaMethodTable)
    val referred = reference("referred", JavaIdentifierTable)

    val instructionNumber = integer("instruction")
    val type = enumeration("type", MemberReferenceType::class)
}

enum class MemberReferenceType {
    CLASS, METHOD, FIELD
}