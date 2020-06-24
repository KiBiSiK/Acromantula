package net.cydhra.acromantula.data.disassembly

import net.cydhra.acromantula.data.FileEntity
import net.cydhra.acromantula.data.FileTable
import net.cydhra.acromantula.data.java.JavaClass
import net.cydhra.acromantula.data.java.JavaClassTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object DisassemblyTable : IntIdTable() {
    val codeFile = reference("codefile", JavaClassTable)
    val disassembly = reference("disassembly", FileTable)
    val created = datetime("created")
}

/**
 * Output of a [net.cydhra.acromantula.features.disassemble.Disassembler]. The disassembly remains valid until the
 * source file is modified.
 */
class Disassembly(entityID: EntityID<Int>) : IntEntity(entityID) {
    companion object : IntEntityClass<Disassembly>(DisassemblyTable)

    var codeFile by JavaClass referencedOn DisassemblyTable.codeFile
    var disassembly by FileEntity referencedOn DisassemblyTable.disassembly
    var created by DisassemblyTable.created
}