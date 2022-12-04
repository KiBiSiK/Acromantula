package net.cydhra.acromantula.features

import net.cydhra.acromantula.features.archives.ArchiveFeature
import net.cydhra.acromantula.features.archives.ZipArchiveType

/**
 * Helper class to initialize features that are supported out-of-the-box without a plugin
 */
object PrefabFeatures {
    fun initialize() {
        ArchiveFeature.registerArchiveType(ZipArchiveType)
    }
}