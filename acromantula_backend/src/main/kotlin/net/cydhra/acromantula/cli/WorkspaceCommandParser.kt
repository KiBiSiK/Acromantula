package net.cydhra.acromantula.cli

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import java.util.*

/**
 * Interface for command argument parsers
 */
interface WorkspaceCommandParser<V> {

    /**
     * Build the workspace command from the command args parser
     */
    fun build(): WorkspaceCommandInterpreter<V>

    /**
     * Report the command result to the console.
     */
    fun report(result: Optional<out Result<V>>)
}