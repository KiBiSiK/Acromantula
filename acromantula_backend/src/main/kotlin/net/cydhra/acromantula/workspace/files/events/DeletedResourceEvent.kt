package net.cydhra.acromantula.workspace.files.events

import net.cydhra.acromantula.bus.Event
import net.cydhra.acromantula.data.FileEntity

/**
 * Fired whenever a resource is deleted or otherwise removed from the workspace
 */
class DeletedResourceEvent(val file: FileEntity) : Event