package net.cydhra.acromantula.bus.events

const val LEVEL_APPLICATION = "application"
const val LEVEL_LIFETIME = "$LEVEL_APPLICATION.lifetime"

const val EVENT_TYPE_STARTUP = "$LEVEL_LIFETIME.startup"
const val EVENT_TYPE_SHUTDOWN = "$LEVEL_LIFETIME.shutdown"