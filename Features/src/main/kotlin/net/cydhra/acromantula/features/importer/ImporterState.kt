package net.cydhra.acromantula.features.importer

/**
 * Marker trait for data classes that importers may use to hold state related to one user-triggered import. The
 * import may involve multiple files, which share the same [ImporterState].
 */
interface ImporterState {}