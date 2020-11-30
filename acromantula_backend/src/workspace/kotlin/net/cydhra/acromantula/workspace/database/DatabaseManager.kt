package net.cydhra.acromantula.workspace.database

import net.cydhra.acromantula.workspace.filesystem.FileEntity

/**
 * A manager for content models. Those are created by plugins to ease access of imported files. For example, imported
 * java class files can be modelled by dumping an index of all of their members into the database, easing access and
 * large scale refactoring of java archives.
 *
 * @param databaseClient the current database client that is used for database transactions
 */
class DatabaseManager internal constructor(private val databaseClient: DatabaseClient) {
    private val registeredContentFactories = mutableMapOf<String, ContentModelFactory<ContentModelTransactionContext>>()

    /**
     * Create a content model for a given file.
     *
     * @param factory id of the factory that shall create the content model
     * @param file file to create the model for
     */
    fun createContentModel(factory: String, file: FileEntity) {

    }

    /**
     * Create a content model with a given [factory] for a given file
     *
     * @param factory factory that shall create the content model
     * @param file file to create the model for
     */
    fun <C : ContentModelTransactionContext> createContentModel(factory: ContentModelFactory<C>, file: FileEntity) {
        factory.createContentModel(file)
    }

    /**
     * Start the modification of a content model in a safe way. A wrapper for database transactions is created that
     * can be used during the modification.
     */
    fun <C : ContentModelTransactionContext> startContentModificationTransaction(
        factory: ContentModelFactory<C>
    ): ContentModelTransaction<C> {
        TODO()
    }

    /**
     * Register a new factory for content models. The factory can be called by front ends or other features using its
     * unique [ContentModelFactory.factoryIdentifier].
     *
     * @param factory (singleton) instance of the factory to register
     */
    fun <C : ContentModelTransactionContext> registerContentModelFactory(factory: ContentModelFactory<C>) {
        if (registeredContentFactories.putIfAbsent(factory.factoryIdentifier, factory) != null) {
            throw IllegalStateException(
                "\"${factory.factoryIdentifier}\" is already registered as a content model " +
                        "factory"
            )
        }
    }
}