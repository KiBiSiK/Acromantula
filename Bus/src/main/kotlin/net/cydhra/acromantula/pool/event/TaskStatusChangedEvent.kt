package net.cydhra.acromantula.pool.event

import net.cydhra.acromantula.bus.Event

data class TaskStatusChangedEvent(val taskId: Int, val newStatus: String) : Event