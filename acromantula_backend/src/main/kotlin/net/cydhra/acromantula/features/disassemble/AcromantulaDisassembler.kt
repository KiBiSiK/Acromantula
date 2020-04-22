package net.cydhra.acromantula.features.disassemble

import net.cydhra.acromantula.features.disassemble.visitor.DisassemblyClassVisitor
import org.objectweb.asm.tree.ClassNode

class AcromantulaDisassembler : Disassembler {
    override val identifier: String = "acromantula"

    override fun disassemble(node: ClassNode) {
        node.accept(DisassemblyClassVisitor())
    }
}