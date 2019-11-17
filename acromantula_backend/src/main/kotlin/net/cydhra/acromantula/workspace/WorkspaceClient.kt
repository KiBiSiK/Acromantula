package net.cydhra.acromantula.workspace

import org.jetbrains.exposed.sql.transactions.TransactionManager

abstract class WorkspaceClient {

    abstract var database: TransactionManager
        protected set

}