package net.cydhra.acromantula.workspace.filesystem.events

import net.cydhra.acromantula.bus.Event
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * Fired whenever content of a resource is updated.
 */
class UpdatedResourceEvent(val file: FileEntity) : Event