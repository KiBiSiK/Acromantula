package net.cydhra.acromantula.bus

/**
 * Any form of event occurring in a [net.cydhra.acromantula.bus.service.Service]. It will be raised by the respective
 * component, dispatched at the [net.cydhra.acromantula.bus.EventBroker] and then handled by all subscribers to the
 * specific event type. Events must be serializable in order to be transferable over a network.
 */
interface Event