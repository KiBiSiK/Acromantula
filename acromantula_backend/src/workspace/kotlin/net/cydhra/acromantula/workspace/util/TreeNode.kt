package net.cydhra.acromantula.workspace.util

import java.util.*

/**
 * A generic tree structure, because for some reason, there is none in Java, Kotlin and Guava. Children and parents
 * are doubly linked, children are stored in a [LinkedList] in their parents.
 */
data class TreeNode<T>(val value: T) {

    private val children = LinkedList<TreeNode<T>>()

    var parent: TreeNode<T>? = null
        private set

    val childList: List<TreeNode<T>> = children

    fun appendChild(child: TreeNode<T>) {
        child.parent = this
        this.children += child
    }
}