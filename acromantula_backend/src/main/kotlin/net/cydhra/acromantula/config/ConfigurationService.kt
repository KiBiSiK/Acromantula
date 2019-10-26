package net.cydhra.acromantula.config

import net.cydhra.acromantula.bus.service.Service

object ConfigurationService : Service {
    override val name: String = "configuration-service"

    override suspend fun initialize() {

    }
}