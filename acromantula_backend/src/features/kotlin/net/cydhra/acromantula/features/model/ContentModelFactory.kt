package net.cydhra.acromantula.features.model

interface ContentModelFactory<C : ContentModelTransactionContext> {

    val factoryIdentifier: String

    fun createContentModelTransactionContext(transaction: ContentModelTransaction): C
}