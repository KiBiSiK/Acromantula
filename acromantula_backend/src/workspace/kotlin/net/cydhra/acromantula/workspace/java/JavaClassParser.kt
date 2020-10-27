package net.cydhra.acromantula.workspace.java

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import net.cydhra.acromantula.workspace.database.DatabaseClient
import net.cydhra.acromantula.workspace.filesystem.FileEntity
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class JavaClassParser {

    private val logger = LogManager.getLogger()

    /**
     * Single thread executor for tasks susceptible for race conditions or database conflicts
     */
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    /**
     * A single threaded executor for critical database accesses, that cannot be done concurrently without violating
     * database constraints
     */
    private val singleThreadExecutorCoroutineContext =
        CoroutineScope(singleThreadExecutor.asCoroutineDispatcher()).coroutineContext

    /**
     * Imports a class and all its members into database.
     *
     * @param byteCode the content of the class file
     * @param databaseClient the database
     * @param classFile a reference to the [FileEntity] containing the bytecode.
     */
    internal suspend fun import(
        byteCode: ByteArray,
        databaseClient: DatabaseClient,
        classFile: FileEntity
    ): JavaClass {
        val classNode = generateClassNode(byteCode)
        logger.trace("parsed class node: \"${classNode.name}\"")

        // retrieve identifier for this class
        val identifier = withContext(singleThreadExecutorCoroutineContext) {
            databaseClient.transaction {
                JavaIdentifier.selectOrInsertClassIdentity(classNode)
            }
        }

        // create class entity
        val javaClassEntity = databaseClient.transaction {
            JavaClass.new {
                this.identifier = identifier
                this.name = classNode.name
                this.accessFlags = classNode.access
                this.signature = classNode.signature
                this.classFile = classFile
            }
        }

        classNode.fields.forEach { fieldNode ->
            // get the field identity entity from DB
            val fieldIdentifierEntity = withContext(singleThreadExecutorCoroutineContext) {
                databaseClient.transaction {
                    JavaIdentifier.selectOrInsertFieldIdentity(classNode, fieldNode)
                }
            }

            // insert the field entity in parallel
            transaction {
                JavaField.new {
                    this.identifier = fieldIdentifierEntity
                    this.owner = javaClassEntity
                    this.name = fieldNode.name
                }
            }
        }

        classNode.methods.forEach { methodNode ->
            // concurrently get the field identity entity from DB
            val methodIdentifierEntity = withContext(singleThreadExecutorCoroutineContext) {
                databaseClient.transaction {
                    JavaIdentifier.selectOrInsertMethodIdentity(classNode, methodNode)
                }
            }

            // insert the method entity in parallel
            val methodEntity = databaseClient.transaction {
                JavaMethod.new {
                    this.identifier = methodIdentifierEntity
                    this.owner = javaClassEntity
                    this.name = methodNode.name
                }
            }
        }

        return javaClassEntity
    }

    private fun generateClassNode(byteCode: ByteArray): ClassNode {
        val node = ClassNode()
        val reader = ClassReader(byteCode)
        reader.accept(node, ClassReader.EXPAND_FRAMES)
        return node
    }

    fun shutdown() {
        logger.info("shutting down single thread executor (timeout 60 seconds)...")
        this.singleThreadExecutor.shutdown()
        if (!this.singleThreadExecutor.awaitTermination(60L, TimeUnit.SECONDS)) {
            logger.info("timeout elapsed. forcing shutdown...")
            this.singleThreadExecutor.shutdownNow()
        }
        logger.info("single thread executor terminated.")
    }
}