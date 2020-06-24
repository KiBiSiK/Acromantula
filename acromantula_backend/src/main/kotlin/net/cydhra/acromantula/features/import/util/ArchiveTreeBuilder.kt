package net.cydhra.acromantula.features.import.util

/**
 * A builder for an archive tree used during archive parsing. There is no guarantee that the zip table is read or
 * even written in correct order to be parsed as a tree directly (i.e. directories are parsed before their contents)
 *
 * This builder stores all files and directories and constructs the tree afterwards.
 *
 * @param archiveName name of the imported archive
 */
class ArchiveTreeBuilder(private val archiveName: String) {

    companion object {
        private const val LAST_DIRECTORY_PATTERN = "[^\\/]*\\/\$"
        private val lastDirectoryRegex = LAST_DIRECTORY_PATTERN.toRegex()
    }

    private val files = mutableListOf<Pair<String, ByteArray>>()
    private val fileMetadata = mutableMapOf<String, List<Pair<String, String>>>()

    /**
     * Add a non-class file to the archive tree
     *
     * @param name file name in zip archive
     * @param content file content
     */
    fun addFileEntry(name: String, content: ByteArray, vararg metadata: Pair<String, String>) {
        files += name to content
        fileMetadata[name] = metadata.toList()
    }

//    /**
//     * Create the archive tree and persist it into the database at the same time
//     *
//     * @return the root element of the new archive tree
//     */
//    fun create(): WorkspaceEntry {
//        assert(TransactionManager.currentOrNull() != null)
//
//        val directoryMap = mutableMapOf<String, WorkspaceEntry>()
//        directoryMap["/"] = net.cydhra.acromantula.files.model.archive.Archive(archiveName)
//
//        val parentMap = mutableMapOf<String, Directory?>()
//        parentMap["/"] = null
//
//        transaction {
//            val archive = Archive.new {
//                name = this@ArchiveTreeBuilder.archiveName
//            }
//
//            this@ArchiveTreeBuilder.files
//                    .forEach { (name, content) ->
//                        val parent = getParentFromDirectoryMap(directoryMap, parentMap, name, archive)
//                        val blob = insertBlobIntoDatabase(name, content, archive,
//                                parentMap[getParentPath(name)], fileMetadata[name]!!)
//
//                        ArchiveFileEntry(
//                                parent = parent,
//                                name = name,
//                                fileType = blob.fileType
//                        ).registerAtParent()
//
//                    }
//
//        }
//
//        return directoryMap["/"]!!
//    }

    /**
     * Get the parent of a given element name form the given directory map
     *
     * @param directoryMap the mutable map where all parent directory relations are saved
     * @param parentMap the mutable map where all parent directory entities are saved
     * @param elementName name of the element, whose parent is searched
     *
     * @return parent of given element
     */
//    private fun getParentFromDirectoryMap(directoryMap: MutableMap<String, WorkspaceEntry>, parentMap: MutableMap<String, Directory?>,
//                                          elementName: String, owningArchive: Archive): WorkspaceEntry {
//        // find the name of the element's parent
//        val parentName = getParentPath(elementName)
//
//        val parentEntry = directoryMap[parentName]
//        if (parentEntry == null) {
//            // insert missing directory into directory map
//            directoryMap[parentName] = ArchiveDirectoryEntry(getParentFromDirectoryMap(directoryMap, parentMap, parentName,
//                    owningArchive), parentName).also { it.registerAtParent() }
//
//            // insert missing directory into database
//            transaction {
//                parentMap[parentName] = Directory.new {
//                    name = parentName
//                    parent = parentMap[getParentPath(parentName)]
//                    archive = owningArchive
//                }
//            }
//        }
//
//        // return the searched parent element
//        return directoryMap[parentName]!!
//    }

    /**
     * Parse the path of a archive element's parent element
     *
     * @param path the element path
     *
     * @return the parent path used in the archive builder maps
     */
    private fun getParentPath(path: String): String {
        return (if (path.endsWith("/"))
            path.replace(lastDirectoryRegex, "")
        else
            path.removeRange(path.lastIndexOf('/').takeIf { it >= 0 }?.let { it + 1 }
                ?: 0, path.length))
            .ifEmpty { "/" }
    }
}