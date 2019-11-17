package net.cydhra.acromantula.bus

/**
 * Common interface to types that are directly serialized to be sent over an IPC channel to other components of the
 * framework. This is especially used for events and requests to ease subscription.
 */
interface IPCSerializable {

    /**
     * A hierarchical descriptor for this serializable component. Hierarchy levels are separated by periods.
     */
    val type: String
}