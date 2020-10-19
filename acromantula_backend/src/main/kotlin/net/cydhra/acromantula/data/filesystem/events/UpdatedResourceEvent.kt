package net.cydhra.acromantula.data.filesystem.events

import net.cydhra.acromantula.bus.Event
import net.cydhra.acromantula.data.filesystem.FileEntity

/**
 * Fired whenever content of a resource is updated.
 */
class UpdatedResourceEvent(val file: FileEntity) : Event