package net.cydhra.acromantula.features.disassemble.visitor

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes.ASM8

class DisassemblyAnnotationVisitor(api: Int = ASM8) : AnnotationVisitor(api)