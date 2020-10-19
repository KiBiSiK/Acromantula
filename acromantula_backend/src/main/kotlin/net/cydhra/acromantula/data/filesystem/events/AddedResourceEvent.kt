package net.cydhra.acromantula.data.filesystem.events

import net.cydhra.acromantula.bus.Event
import net.cydhra.acromantula.data.filesystem.FileEntity

/**
 * Fired whenever a new resource is imported or otherwise added into the workspace
 * @param file the file entity added into the workspace
 */
class AddedResourceEvent(val file: FileEntity) : Event