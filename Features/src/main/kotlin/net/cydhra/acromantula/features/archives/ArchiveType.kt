package net.cydhra.acromantula.features.archives

import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * An archive type added by a plugin. This usually goes alongside an importer (and optional exporter) strategy for
 * this archive format. This format defines which operations can be performed within the archive (such as moving
 * files, adding or deleting them, etc).
 */
interface ArchiveType {

    /**
     * A unique file type identifier to mark archives with, so the [ArchiveFeature] can find the type for an existing
     * archive
     */
    val fileTypeIdentifier: String

    /**
     * Whether this archive type supports adding new files
     */
    fun canAddFile(): Boolean

    /**
     * Called after a file has been added to an archive of this type.
     *
     * @param archive modified archive
     * @param fileEntity added file
     */
    fun onFileAdded(archive: FileEntity, fileEntity: FileEntity)

    /**
     * Whether this archive type supports moving its files
     */
    fun canMoveFile(): Boolean

    /**
     * Called after a file has been moved within an archive of this type. If the file has been moved from the
     * outside, [onFileAdded] is called instead. If the file has been moved out of this archive, [onFileDeleted] is
     * called instead.
     *
     * @param archive modified archive
     * @param source the directory where the file originally resided. May be equal to the archive file, if the file
     * was at top-level
     * @param file the moved file (with its new parent directory already correctly set)
     */
    fun onFileMoved(archive: FileEntity, source: FileEntity, file: FileEntity)

    /**
     * Whether this archive supports renaming of files and whether the given name is a valid file name for this
     * archive format
     */
    fun canRenameFile(name: String): Boolean

    /**
     * Called before a file is renamed. The file still has its old name, and will be renamed after this method call.
     *
     * @param archive modified archive
     * @param file modified file
     * @param newName new file name
     */
    fun onFileRename(archive: FileEntity, file: FileEntity, newName: String)

    /**
     * Whether this archive type supports deleting files
     */
    fun canDeleteFile(): Boolean

    /**
     * Called directly before a file is being deleted from an archive of this type (or moved out of the archive). The
     * file has not been deleted yet, but will be deleted after this method call
     *
     * @param archive the archive that is being modified
     * @param file the file that is going to be deleted
     */
    fun onFileDelete(archive: FileEntity, file: FileEntity)

    /**
     * Whether this archive type supports directories
     */
    fun canAddDirectory(): Boolean

    /**
     * Called after a directory has been added to an archive of this type.
     *
     * @param archive modified archive
     * @param directory added directory
     */
    fun onDirectoryAdded(archive: FileEntity, directory: FileEntity)

    /**
     * Whether this archive type can be created from scratch. This will allow a user to create a directory and mark
     * is as an archive of this format, so that subsequent actions are subject to the capabilities of this archive type.
     */
    fun canCreateArchiveFromScratch(): Boolean

    /**
     * Mark the given (empty) directory as an archive of this type
     */
    fun createArchiveFromScratch(directory: FileEntity)
}