package net.cydhra.acromantula.data.filesystem.events

import net.cydhra.acromantula.bus.Event
import net.cydhra.acromantula.data.filesystem.FileEntity

/**
 * Fired whenever a resource is deleted or otherwise removed from the workspace
 */
class DeletedResourceEvent(val file: FileEntity) : Event