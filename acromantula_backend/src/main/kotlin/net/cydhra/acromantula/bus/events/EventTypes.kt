package net.cydhra.acromantula.bus.events

import net.cydhra.acromantula.bus.ChildEventChannel
import net.cydhra.acromantula.bus.RootEventChannel

val EVENT_CHANNEL_APPLICATION = RootEventChannel("application")
val EVENT_CHANNEL_APPLICATION_LIFETIME = ChildEventChannel(EVENT_CHANNEL_APPLICATION, "lifetime")

val EVENT_CHANNEL_STARTUP = ChildEventChannel(EVENT_CHANNEL_APPLICATION_LIFETIME, "startup")
val EVENT_CHANNEL_SHUTDOWN = ChildEventChannel(EVENT_CHANNEL_APPLICATION_LIFETIME, "shutdown")