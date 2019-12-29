package net.cydhra.acromantula.bus.events

import net.cydhra.acromantula.bus.Event

/**
 * Event fired after plugins are loaded and services have been registered.
 */
class ApplicationStartupEvent : Event {
    override val channel = EVENT_CHANNEL_STARTUP
}