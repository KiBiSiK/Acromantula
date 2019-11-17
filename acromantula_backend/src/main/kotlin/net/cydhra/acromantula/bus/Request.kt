package net.cydhra.acromantula.bus

/**
 * An event type, that can be sent to the broker, is dispatched and after dispatching, yields a result. This event
 * type must not be handled by more than one handler. This is a loose way of requesting resources without knowing the
 * sub-system that can deliver this resource.
 *
 * @param T the requested data type
 */
interface Request<T> : IPCSerializable {

    fun fulfil(data: T)
}