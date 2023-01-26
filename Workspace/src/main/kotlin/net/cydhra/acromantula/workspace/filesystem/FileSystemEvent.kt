package net.cydhra.acromantula.workspace.filesystem

sealed class FileSystemEvent {

    /**
     * Fired by [WorkspaceFileSystem.createFile]
     */
    class FileCreatedEvent(val fileEntity: FileEntity) : FileSystemEvent()

    class FileUpdatedEvent : FileSystemEvent()

    class FileRenamedEvent : FileSystemEvent()

    class FileMovedEvent : FileSystemEvent()

    class FileDeletedEvent : FileSystemEvent()

    class ViewCreatedEvent : FileSystemEvent()

    class ArchiveCreatedEvent : FileSystemEvent()
}