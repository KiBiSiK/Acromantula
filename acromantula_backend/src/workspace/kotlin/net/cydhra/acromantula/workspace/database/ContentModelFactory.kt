package net.cydhra.acromantula.workspace.database

import net.cydhra.acromantula.workspace.filesystem.FileEntity

interface ContentModelFactory<out C : ContentModelTransactionContext> {

    val factoryIdentifier: String

    /**
     * Create a new context for a content model transaction.
     */
    fun createTransactionContext(): C

    /**
     * Create a content model for the given [fileEntity] and insert it into database
     */
    fun createContentModel(fileEntity: FileEntity)
}