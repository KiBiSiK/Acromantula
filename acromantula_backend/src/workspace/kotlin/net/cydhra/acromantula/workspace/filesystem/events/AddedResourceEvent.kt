package net.cydhra.acromantula.workspace.filesystem.events

import net.cydhra.acromantula.bus.Event
import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * Fired whenever a new resource is imported or otherwise added into the workspace
 *
 * @param file the file entity added into the workspace
 * @param content binary file content
 */
class AddedResourceEvent(val file: FileEntity, val content: ByteArray) : Event