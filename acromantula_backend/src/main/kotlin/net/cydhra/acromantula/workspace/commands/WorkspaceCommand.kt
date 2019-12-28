package net.cydhra.acromantula.workspace.commands

import net.cydhra.acromantula.bus.IPCSerializable

/**
 * A command for the [CommandDispatcher] that invokes some kind of service within the Workspace.
 */
interface WorkspaceCommand : IPCSerializable {

}