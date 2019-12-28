package net.cydhra.acromantula.bus.serialization

import net.cydhra.acromantula.bus.Event

/**
 * Events are serializable because they are sent though IPC channels and maybe manually triggered by CLIs. This class
 * is a helper to serialize and deserialize them.
 */
class EventSerializer() {

    fun serializeEvent(event: Event): String {
        TODO()
    }

    fun deserializeEvent(event: String): Event {
        TODO()
    }
}