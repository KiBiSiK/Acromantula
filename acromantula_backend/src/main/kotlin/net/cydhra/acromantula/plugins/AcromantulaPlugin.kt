package net.cydhra.acromantula.plugins

/**
 * Interface for plugins
 */
interface AcromantulaPlugin {
    val name: String
    val author: String

    fun initialize()
}