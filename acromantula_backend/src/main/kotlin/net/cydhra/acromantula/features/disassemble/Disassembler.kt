package net.cydhra.acromantula.features.disassemble

import org.objectweb.asm.tree.ClassNode

interface Disassembler {
    /**
     * A unique identifier for the disassembler that is later used to reference it in GUIs and commands. It must not
     * contain spaces.
     */
    val identifier: String

    /**
     * Disassemble a class node into text and meta information
     */
    fun disassemble(node: ClassNode)
}