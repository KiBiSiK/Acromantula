package net.cydhra.acromantula.cli

import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter

/**
 * Interface for command argument parsers
 */
interface WorkspaceCommandArgs {

    /**
     * Build the workspace command from the command args parser
     */
    fun build(): WorkspaceCommandInterpreter
}