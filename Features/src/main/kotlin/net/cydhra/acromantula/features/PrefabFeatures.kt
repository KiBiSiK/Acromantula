package net.cydhra.acromantula.features

import net.cydhra.acromantula.features.archives.ArchiveFeature
import net.cydhra.acromantula.features.archives.ZipArchiveType
import net.cydhra.acromantula.features.view.GenerateViewFeature
import net.cydhra.acromantula.features.view.prefabs.InspectPortableNetworkGraphics

/**
 * Helper class to initialize features that are supported out-of-the-box without a plugin
 */
object PrefabFeatures {
    fun initialize() {
        ArchiveFeature.registerArchiveType(ZipArchiveType)
        GenerateViewFeature.registerViewGenerator(InspectPortableNetworkGraphics)
    }
}