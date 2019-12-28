package net.cydhra.acromantula.workspace.commands

import net.cydhra.acromantula.bus.Event

/**
 * A command for the [CommandDispatcher] that invokes some kind of service within the Workspace. A command is a
 * special kind of [Event].
 */
interface WorkspaceCommand : Event