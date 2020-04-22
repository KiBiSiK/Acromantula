package net.cydhra.acromantula.features.disassemble.visitor

import org.objectweb.asm.ModuleVisitor
import org.objectweb.asm.Opcodes.ASM8

class DisassemblyModuleVisitor(api: Int = ASM8) : ModuleVisitor(api) {
    override fun visitEnd() {
        super.visitEnd()
    }

    override fun visitProvide(service: String?, vararg providers: String?) {
        super.visitProvide(service, *providers)
    }

    override fun visitExport(packaze: String?, access: Int, vararg modules: String?) {
        super.visitExport(packaze, access, *modules)
    }

    override fun visitMainClass(mainClass: String?) {
        super.visitMainClass(mainClass)
    }

    override fun visitOpen(packaze: String?, access: Int, vararg modules: String?) {
        super.visitOpen(packaze, access, *modules)
    }

    override fun visitRequire(module: String?, access: Int, version: String?) {
        super.visitRequire(module, access, version)
    }

    override fun visitPackage(packaze: String?) {
        super.visitPackage(packaze)
    }

    override fun visitUse(service: String?) {
        super.visitUse(service)
    }
}