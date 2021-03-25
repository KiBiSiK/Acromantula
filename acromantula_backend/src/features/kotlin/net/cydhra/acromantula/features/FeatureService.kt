package net.cydhra.acromantula.features

import net.cydhra.acromantula.bus.Service

/**
 * This service is used to register event handlers for features
 */
object FeatureService : Service {
    override val name: String = "feature-service"

    override suspend fun initialize() {

    }
}