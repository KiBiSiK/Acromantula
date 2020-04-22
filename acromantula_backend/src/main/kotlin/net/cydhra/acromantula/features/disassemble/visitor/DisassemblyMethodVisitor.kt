package net.cydhra.acromantula.features.disassemble.visitor

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.ASM8

class DisassemblyMethodVisitor(api: Int = ASM8) : MethodVisitor(api) {
    override fun visitMultiANewArrayInsn(descriptor: String, numDimensions: Int) {
        super.visitMultiANewArrayInsn(descriptor, numDimensions)
    }

    override fun visitFrame(type: Int, numLocal: Int, local: Array<out Any>, numStack: Int, stack: Array<out Any>) {
        super.visitFrame(type, numLocal, local, numStack, stack)
    }

    override fun visitVarInsn(opcode: Int, `var`: Int) {
        super.visitVarInsn(opcode, `var`)
    }

    override fun visitTryCatchBlock(start: Label, end: Label, handler: Label, type: String) {
        super.visitTryCatchBlock(start, end, handler, type)
    }

    override fun visitLookupSwitchInsn(dflt: Label, keys: IntArray, labels: Array<out Label>) {
        super.visitLookupSwitchInsn(dflt, keys, labels)
    }

    override fun visitJumpInsn(opcode: Int, label: Label) {
        super.visitJumpInsn(opcode, label)
    }

    override fun visitLdcInsn(value: Any) {
        super.visitLdcInsn(value)
    }

    override fun visitAnnotableParameterCount(parameterCount: Int, visible: Boolean) {
        super.visitAnnotableParameterCount(parameterCount, visible)
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        super.visitIntInsn(opcode, operand)
    }

    override fun visitTypeInsn(opcode: Int, type: String) {
        super.visitTypeInsn(opcode, type)
    }

    override fun visitAnnotationDefault(): AnnotationVisitor {
        return super.visitAnnotationDefault()
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath,
        descriptor: String,
        visible: Boolean
    ): AnnotationVisitor {
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        super.visitMaxs(maxStack, maxLocals)
    }

    override fun visitInvokeDynamicInsn(
        name: String,
        descriptor: String,
        bootstrapMethodHandle: Handle,
        vararg bootstrapMethodArguments: Any
    ) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments)
    }

    override fun visitLabel(label: Label) {
        super.visitLabel(label)
    }

    override fun visitTryCatchAnnotation(
        typeRef: Int,
        typePath: TypePath,
        descriptor: String,
        visible: Boolean
    ): AnnotationVisitor {
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    override fun visitInsn(opcode: Int) {
        super.visitInsn(opcode)
    }

    override fun visitInsnAnnotation(
        typeRef: Int,
        typePath: TypePath,
        descriptor: String,
        visible: Boolean
    ): AnnotationVisitor {
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitParameterAnnotation(parameter: Int, descriptor: String, visible: Boolean): AnnotationVisitor {
        return super.visitParameterAnnotation(parameter, descriptor, visible)
    }

    override fun visitIincInsn(`var`: Int, increment: Int) {
        super.visitIincInsn(`var`, increment)
    }

    override fun visitLineNumber(line: Int, start: Label) {
        super.visitLineNumber(line, start)
    }

    override fun visitLocalVariableAnnotation(
        typeRef: Int,
        typePath: TypePath,
        start: Array<out Label>,
        end: Array<out Label>,
        index: IntArray,
        descriptor: String,
        visible: Boolean
    ): AnnotationVisitor {
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible)
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label, vararg labels: Label) {
        super.visitTableSwitchInsn(min, max, dflt, *labels)
    }

    override fun visitEnd() {
        super.visitEnd()
    }

    override fun visitLocalVariable(
        name: String,
        descriptor: String,
        signature: String,
        start: Label,
        end: Label,
        index: Int
    ) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index)
    }

    override fun visitParameter(name: String, access: Int) {
        super.visitParameter(name, access)
    }

    override fun visitAttribute(attribute: Attribute) {
        super.visitAttribute(attribute)
    }

    override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitCode() {
        super.visitCode()
    }
}