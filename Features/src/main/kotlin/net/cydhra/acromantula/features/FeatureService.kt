package net.cydhra.acromantula.features

import net.cydhra.acromantula.bus.EventBroker
import net.cydhra.acromantula.bus.Service
import net.cydhra.acromantula.features.mapper.MapperFeature
import net.cydhra.acromantula.workspace.filesystem.events.AddedResourceEvent

/**
 * This service is used to register event handlers for features
 */
object FeatureService : Service {
    override val name: String = "feature-service"

    override suspend fun initialize() {
        EventBroker.registerEventListener(AddedResourceEvent::class, MapperFeature::onFileAdded)
    }
}