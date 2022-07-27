package net.cydhra.acromantula.plugins

import java.io.File
import java.net.URLClassLoader
import java.util.*

/**
 * Facade service for the plugin sub-system. Plugin loading and management is delegated from here.
 */
object PluginService {

    /**
     * The folder where plugin JARs are located
     */
    private val pluginsFolder = File("plugins").apply { mkdir() }

    /**
     * A list of all loaded plugins
     */
    private val loadedPlugins = mutableListOf<AcromantulaPlugin>()

    /**
     * Initialize all resources.
     */
    fun initialize() {
        val classLoader = URLClassLoader(
            (pluginsFolder.listFiles() ?: emptyArray())
                .filter { it.name.endsWith("jar") }
                .map { it.toURI().toURL() }
                .toTypedArray(), this.javaClass.classLoader)
        val pluginLoader = ServiceLoader.load(AcromantulaPlugin::class.java, classLoader)
        pluginLoader.iterator().forEach { plugin ->
            loadedPlugins += plugin.apply(AcromantulaPlugin::initialize)
        }
    }

    /**
     * Get a list of all loaded [AcromantulaPlugin]s
     */
    fun listPlugins(): List<AcromantulaPlugin> {
        return this.loadedPlugins
    }
}