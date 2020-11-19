package net.cydhra.acromantula.workspace.filesystem

/**
 * A file type classifies what operations can be formed upon a file. The type is specified similar to MIME types,
 * however the hierarchy does not comply with MIME specifications. Each file type can specify a file extension that
 * can be used by tools that generate this file type.
 *
 * @param typeHierarchy a hierarchy of type specifiers. The base type is `binary`, all types must derive from it.
 * Then different type class identifiers like `text`, `image` or `audio` can follow, so the frontend knows that type
 * specific tools can be used with it. Third should be a classifier for this specific type, although a fourth class
 * may exist for subtypes. A full example might my `bin/image/png`.
 */
data class FileType(val typeHierarchy: String, val fileExtension: String?) {

    /**
     * Convenience method to derive a specific type from another by appending its file extension to the type hierarchy
     */
    operator fun plus(extension: String) = FileType(this.typeHierarchy + "/" + extension, extension)
}

val binaryFileType = FileType("bin", null)

val imageFileType = FileType("bin/image", null)
val bitmapFileType = imageFileType + "bmp"
val pngFileType = imageFileType + "png"

val textFileType = FileType("bin/text", "txt")

val audioFileType = FileType("bin/audio", null)

val archiveFileType = FileType("bin/archive", null)
val zipFileType = archiveFileType + "zip"