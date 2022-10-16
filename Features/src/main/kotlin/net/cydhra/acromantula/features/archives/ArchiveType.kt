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
     * Whether this archive type supports moving its files
     */
    fun canMoveFile(): Boolean

    /**
     * Whether this archive type supports deleting files
     */
    fun canDeleteFile(): Boolean

    /**
     * Whether this archive type supports directories
     */
    fun canAddDirectory(): Boolean

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