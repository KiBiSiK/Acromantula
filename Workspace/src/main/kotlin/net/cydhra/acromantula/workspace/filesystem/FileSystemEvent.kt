package net.cydhra.acromantula.workspace.filesystem

import net.cydhra.acromantula.workspace.disassembly.FileViewEntity

sealed class FileSystemEvent {

    /**
     * Fired by [WorkspaceFileSystem.createFile]
     */
    class FileCreatedEvent(val fileEntity: FileEntity) : FileSystemEvent()

    class FileUpdatedEvent : FileSystemEvent()

    class FileRenamedEvent(val fileEntity: FileEntity, val oldName: String) : FileSystemEvent()

    class FileMovedEvent(val fileEntity: FileEntity, val oldParent: FileEntity?) : FileSystemEvent()

    class FileDeletedEvent(val fileEntity: FileEntity) : FileSystemEvent()

    class ViewCreatedEvent(val fileEntity: FileEntity, val viewEntity: FileViewEntity) : FileSystemEvent()

    class ArchiveCreatedEvent(val fileEntity: FileEntity) : FileSystemEvent()
}