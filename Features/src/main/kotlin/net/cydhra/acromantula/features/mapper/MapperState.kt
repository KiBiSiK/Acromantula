package net.cydhra.acromantula.features.mapper

/**
 * Marker trait for data classes that mappers may use to hold state related to one user-triggered [MapperJob].
 */
interface MapperState {

    /**
     * Called when the mapper job is no longer accepting new files and is supposed to finish mapping. This method
     * returns once mapping is done.
     */
    suspend fun onFinishMapping()
}