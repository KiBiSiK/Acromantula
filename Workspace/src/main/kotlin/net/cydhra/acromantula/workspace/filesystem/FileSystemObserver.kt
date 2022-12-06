package net.cydhra.acromantula.workspace.filesystem

interface FileSystemObserver {

    /**
     * Called after a new file has been added to the workspace. Called for every file, even if a whole archive is
     * imported.
     */
    suspend fun onFileCreated(event: FileSystemEvent.FileCreatedEvent)

    /**
     * Called after a file's content has been modified. All file views have been invalidated implicitly during this
     * action.
     */
    suspend fun onFileUpdated(event: FileSystemEvent.FileUpdatedEvent)

    /**
     * Called after a new file has been renamed. Does not apply if the file has been moved.
     */
    suspend fun onFileRenamed(event: FileSystemEvent.FileRenamedEvent)

    /**
     * Called after a new file has been moved to another parent file. Does not apply if the file has been renamed.
     */
    suspend fun onFileMoved(event: FileSystemEvent.FileMovedEvent)

    /**
     * Called after a new file has been deleted. Called for each file individually, even if a whole archive/directory
     * is deleted.
     */
    suspend fun onFileDeleted(event: FileSystemEvent.FileDeletedEvent)

    /**
     * Called after a new view has been created for a file. The view is available until the file is updated or deleted.
     */
    suspend fun onViewCreated(event: FileSystemEvent.ViewCreatedEvent)

    /**
     * Called after a file has been marked as an archive. The file does already exist at this point, it is only
     * converted into an archive.
     */
    suspend fun onArchiveCreated(event: FileSystemEvent.ArchiveCreatedEvent)
}