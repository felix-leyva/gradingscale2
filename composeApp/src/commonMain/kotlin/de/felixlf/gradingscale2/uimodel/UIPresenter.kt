package de.felixlf.gradingscale2.uimodel

import kotlinx.coroutines.flow.MutableSharedFlow

interface UIEventPresenter<UIEvent> {
    val events: MutableSharedFlow<UIEvent>
    fun onEvent(event: UIEvent)
}

class UIEventPresenterImpl<UIEvent> : UIEventPresenter<UIEvent> {
    // Events have a capacity large enough to handle simultaneous UI events, but
    // small enough to surface issues if they get backed up for some reason.
    override val events = MutableSharedFlow<UIEvent>(extraBufferCapacity = 20)
    override fun onEvent(event: UIEvent) {
        if (!events.tryEmit(event)) {
            error("Event buffer overflow.")
        }
    }
}
