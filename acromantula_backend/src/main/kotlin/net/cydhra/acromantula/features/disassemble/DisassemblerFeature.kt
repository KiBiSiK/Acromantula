package net.cydhra.acromantula.features.disassemble

import net.cydhra.acromantula.database.disassembly.Disassembly
import sun.plugin.com.JavaClass

/**
 * Provides the disassembly features for the application. Multiple disassemblers can be registered.
 */
object DisassemblerFeature {

    /**
     * Map of all registered disassemblers accessible by name
     */
    private val disassemblers = mutableMapOf<String, Disassembler>()


    /**
     * Register a [Disassembler] at the service.
     *
     * @param disassembler implementation of [Disassembler] interface
     *
     * @throws IllegalArgumentException if a disassembler with the same [Disassembler.identifier] has already been
     * registered
     */
    fun registerDisassembler(disassembler: Disassembler) {
        if (this.disassemblers.containsKey(disassembler.identifier)) {
            throw IllegalArgumentException("cannot register two disassemblers with the same name")
        }

        this.disassemblers[disassembler.identifier] = disassembler
    }

    /**
     * Provide disassembly for a java class. It may be generated or provided by file
     *
     * @param classFile a java class file entity
     * @param disassembler name of the disassembler to use
     *
     * @throws IllegalArgumentException if the named disassembler is unknown
     */
    suspend fun provideDisassembly(classFile: JavaClass, disassembler: String): Disassembly {
        if (!this.disassemblers.containsKey(disassembler)) {
            throw IllegalArgumentException("disassembler named \"$disassembler\" cannot be found")
        }

        TODO("not implemented")
    }
}