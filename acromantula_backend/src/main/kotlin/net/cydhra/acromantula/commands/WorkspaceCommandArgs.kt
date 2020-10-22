package net.cydhra.acromantula.commands

/**
 * Interface for command argument parsers
 */
interface WorkspaceCommandArgs {

    /**
     * Build the workspace command from the command args parser
     */
    fun build(): WorkspaceCommand
}