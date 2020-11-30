package net.cydhra.acromantula.workspace.database

class ContentModelTransaction<C> internal constructor(private val context: C) {

    /**
     * Modify the content model and commit the modification into database
     */
    fun modifyContentModel(modification: C.() -> Unit) {
        TODO()
    }

    internal fun beginTransaction() {
        TODO()
    }

    internal fun endTransaction() {
        TODO()
    }
}