package net.cydhra.acromantula.bus.events

import net.cydhra.acromantula.bus.Event

/**
 * Event fired when the server shuts down.
 */
class ApplicationShutdownEvent : Event {
    override val type: String = EVENT_TYPE_SHUTDOWN
}