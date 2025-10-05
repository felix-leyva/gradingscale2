package de.felixlf.gradingscale2.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.browser.window
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import kotlin.js.ExperimentalWasmJsInterop

private object TabNavigationRegistry {
    private val handlers = mutableMapOf<Int, TabHandler>()
    private var nextId = 0
    private var listenerAttached = false
    private var pendingAction: (() -> Unit)? = null

    data class TabHandler(
        val onTab: (() -> Boolean)?,
        val onShiftTab: (() -> Boolean)?,
        var isFocused: Boolean = false,
    )

    fun executePendingAction() {
        pendingAction?.invoke()
        pendingAction = null
    }

    fun register(onTab: (() -> Boolean)?, onShiftTab: (() -> Boolean)?): Int {
        val id = nextId++
        handlers[id] = TabHandler(onTab, onShiftTab)
        ensureListenerAttached()
        return id
    }

    fun unregister(id: Int) {
        handlers.remove(id)
    }

    fun updateHandlers(id: Int, onTab: (() -> Boolean)?, onShiftTab: (() -> Boolean)?) {
        handlers[id]?.let { handler ->
            handlers[id] = handler.copy(onTab = onTab, onShiftTab = onShiftTab)
        }
    }

    fun updateFocus(id: Int, focused: Boolean) {
        // When a field gains focus, ensure all others lose focus
        if (focused) {
            handlers.values.forEach { it.isFocused = false }
        }
        handlers[id]?.isFocused = focused
    }

    private fun ensureListenerAttached() {
        if (!listenerAttached) {
            window.addEventListener("keydown", ::handleKeyDown)
            listenerAttached = true
        }
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    private fun handleKeyDown(event: Event) {
        val keyboardEvent = event as KeyboardEvent
        if (keyboardEvent.key == "Tab") {
            // Find the currently focused handler
            val focusedHandler = handlers.values.find { it.isFocused }

            if (focusedHandler != null) {
                // Check which handler to invoke based on Shift key
                val handlerExists = if (keyboardEvent.shiftKey) {
                    focusedHandler.onShiftTab != null
                } else {
                    focusedHandler.onTab != null
                }

                // If a handler exists for this direction, prevent default and invoke it
                if (handlerExists) {
                    keyboardEvent.preventDefault()
                    keyboardEvent.stopPropagation()

                    // Defer the focus change to the next event loop tick
                    // This is necessary in wasmJs to ensure the focus change happens
                    // after the current event is fully processed
                    window.setTimeout(
                        handler = {
                            if (keyboardEvent.shiftKey) {
                                focusedHandler.onShiftTab?.invoke()
                            } else {
                                focusedHandler.onTab?.invoke()
                            }
                            null
                        },
                        timeout = 0,
                    )
                }
            }
        }
    }
}

@Composable
actual fun Modifier.onPreviewTab(): Modifier {
    val focusManager = LocalFocusManager.current
    val handlerId = remember {
        TabNavigationRegistry.register(
            onTab = { focusManager.moveFocus(FocusDirection.Next) },
            onShiftTab = { focusManager.moveFocus(FocusDirection.Previous) },
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            TabNavigationRegistry.unregister(handlerId)
        }
    }

    return this.onFocusChanged { focusState ->
        TabNavigationRegistry.updateFocus(handlerId, focusState.isFocused)
    }
}
