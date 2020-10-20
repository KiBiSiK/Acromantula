package net.cydhra.acromantula.workspace.java

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

const val MAX_IDENTIFIER_LENGTH: Int = Short.MAX_VALUE.toInt()

internal object JavaIdentifierTable : IntIdTable("JavaIdentifier") {
    val identifier = varchar("identifier", MAX_IDENTIFIER_LENGTH).uniqueIndex()
}

/**
 * A unique identifier for any java member
 */
class JavaIdentifier(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<JavaIdentifier>(JavaIdentifierTable) {
        /**
         * Retrieves a unique identifier for the given [ClassNode] from database or inserts and returns a new one, if
         * this class is unknown.
         */
        fun selectOrInsertClassIdentity(classNode: ClassNode): JavaIdentifier {
            val identity = constructClassIdentity(classNode)
            return find { JavaIdentifierTable.identifier eq identity }.firstOrNull()
                ?: JavaIdentifier.new { identifier = identity }
        }

        fun selectOrInsertFieldIdentity(owner: ClassNode, fieldNode: FieldNode): JavaIdentifier {
            val identity = constructFieldIdentity(owner, fieldNode)
            return find { JavaIdentifierTable.identifier eq identity }.firstOrNull()
                ?: JavaIdentifier.new { identifier = identity }
        }

        fun selectOrInsertMethodIdentity(owner: ClassNode, methodNode: MethodNode): JavaIdentifier {
            val identity = constructMethodIdentity(owner, methodNode)
            return find { JavaIdentifierTable.identifier eq identity }.firstOrNull()
                ?: JavaIdentifier.new { identifier = identity }
        }

        private fun constructClassIdentity(classNode: ClassNode): String {
            return classNode.name
        }

        private fun constructFieldIdentity(owner: ClassNode, fieldNode: FieldNode): String {
            return "${constructClassIdentity(owner)}::${fieldNode.name}:${fieldNode.desc}"
        }

        private fun constructMethodIdentity(owner: ClassNode, methodNode: MethodNode): String {
            return "${constructClassIdentity(owner)}::${methodNode.name}${methodNode.desc}"
        }
    }

    var identifier by JavaIdentifierTable.identifier
        private set
}