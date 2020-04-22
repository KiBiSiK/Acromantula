package net.cydhra.acromantula.features.disassemble.visitor

import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes.ASM8

class DisassemblyFieldVisitor(api: Int = ASM8) : FieldVisitor(api)