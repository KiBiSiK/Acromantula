package net.cydhra.acromantula.workspace.files.events

import net.cydhra.acromantula.bus.Event
import net.cydhra.acromantula.database.FileEntity

/**
 * Fired whenever content of a resource is updated.
 */
class UpdatedResourceEvent(val file: FileEntity) : Event