package net.cydhra.acromantula.pool.event

import net.cydhra.acromantula.bus.Event

data class TaskFinishedEvent(val taskId: Int) : Event