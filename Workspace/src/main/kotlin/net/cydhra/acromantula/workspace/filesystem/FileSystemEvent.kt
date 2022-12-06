package net.cydhra.acromantula.workspace.filesystem

sealed class FileSystemEvent {

    class FileCreatedEvent : FileSystemEvent()

    class FileUpdatedEvent : FileSystemEvent()

    class FileRenamedEvent : FileSystemEvent()

    class FileMovedEvent : FileSystemEvent()

    class FileDeletedEvent : FileSystemEvent()

    class ViewCreatedEvent : FileSystemEvent()

    class ArchiveCreatedEvent : FileSystemEvent()
}