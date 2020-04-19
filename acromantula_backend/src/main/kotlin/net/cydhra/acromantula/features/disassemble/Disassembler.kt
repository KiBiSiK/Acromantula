package net.cydhra.acromantula.features.disassemble

interface Disassembler {
    /**
     * A unique identifier for the disassembler that is later used to reference it in GUIs and commands. It must not
     * contain spaces.
     */
    val identifier: String
}