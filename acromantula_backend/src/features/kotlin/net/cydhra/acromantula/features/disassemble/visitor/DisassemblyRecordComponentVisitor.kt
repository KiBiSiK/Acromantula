package net.cydhra.acromantula.features.disassemble.visitor

import org.objectweb.asm.Opcodes.ASM8
import org.objectweb.asm.RecordComponentVisitor

class DisassemblyRecordComponentVisitor(api: Int = ASM8) : RecordComponentVisitor(api)