package net.cydhra.acromantula.commands.interpreters

import net.cydhra.acromantula.cli.CommandLineService
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter

/**
 * Command to list all available commands
 */
class ListCommandsCommandInterpreter() : WorkspaceCommandInterpreter<List<String>> {

    override suspend fun evaluate() = CommandLineService.commands
}

