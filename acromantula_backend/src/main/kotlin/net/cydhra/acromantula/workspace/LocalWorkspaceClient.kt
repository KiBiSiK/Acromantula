package net.cydhra.acromantula.workspace

import org.jetbrains.exposed.sql.transactions.TransactionManager

class LocalWorkspaceClient() : WorkspaceClient() {
    override var database: TransactionManager
        get() = TODO("not implemented")
        set(value) {}
}