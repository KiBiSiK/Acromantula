package net.cydhra.acromantula.bus

/**
 * Hierarchical classification of events that allows fine granular subscriptions to event channels.
 */
sealed class EventChannel {

    /**
     * Get a string based representation of this type
     */
    abstract fun getStringRepresentation(): String

    override fun toString(): String {
        return getStringRepresentation()
    }
}

/**
 * Root event channel
 *
 * @param classifier the root channel name
 */
data class RootEventChannel(private val classifier: String) : EventChannel() {
    override fun getStringRepresentation(): String {
        return classifier
    }
}

/**
 * Subchannel of a [parent] channel
 *
 * @param parent parent event channel
 * @param classifier name of this event channel
 */
data class ChildEventChannel(private val parent: EventChannel, private val classifier: String) : EventChannel() {
    override fun getStringRepresentation(): String {
        return "${parent.getStringRepresentation()}.$classifier"
    }
}