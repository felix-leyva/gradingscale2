package de.felixlf.gradingscale2.entities.uimodel

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlin.uuid.Uuid

interface SingleEventType

data class SingleTimeEvent<T : SingleEventType>(
    val uuid: Uuid,
    val event: T,
)

class EventsQueue<T : SingleEventType> {
    var events: ImmutableMap<Uuid, SingleTimeEvent<T>>
        field = persistentMapOf<Uuid, SingleTimeEvent<T>>()
        private set(value) {
            field = value.toPersistentMap()
        }

    fun add(event: T): EventsQueue<T> {
        val singleTimeEvent = SingleTimeEvent(Uuid.random(), event)
        events = events.put(singleTimeEvent.uuid, singleTimeEvent)
        return this
    }

    fun remove(event: SingleTimeEvent<T>): EventsQueue<T> {
        events = events.remove(event.uuid)
        return this
    }

    inline fun <reified R> removeLastOfType(): EventsQueue<T> {
        events.values.findLast { it.event is R }?.let { this.remove(it) }
        return this
    }

    fun removeAll(): EventsQueue<T> {
        events = events.clear()
        return this
    }

    inline fun <reified R> ofType(): R? = events.values.firstOrNull { it.event is R }?.event as R
}

sealed interface ImportEvent : SingleEventType {
    data object ShowSaved : ImportEvent
}

val test = EventsQueue<ImportEvent>()

val event = test.ofType<ImportEvent.ShowSaved>()
