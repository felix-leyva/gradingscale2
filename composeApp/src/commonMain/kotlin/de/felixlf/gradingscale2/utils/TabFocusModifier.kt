package de.felixlf.gradingscale2.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Intercepts Tab key presses and moves focus according to the focus order
 * defined by focusProperties modifiers.
 */
@Composable
expect fun Modifier.onPreviewTab(): Modifier

/**
 * Internal implementation of Tab focus handling using onPreviewKeyEvent.
 * This is used by JVM, Android, and iOS platforms.
 * Uses FocusManager to respect the focus order defined by focusProperties.
 */
@Composable
internal fun Modifier.onPreviewTabInternal(): Modifier {
    val focusManager = LocalFocusManager.current
    var isShiftPressed by remember { mutableStateOf(false) }

    return this.onPreviewKeyEvent { keyEvent ->
        if (keyEvent.key != Key.Tab) return@onPreviewKeyEvent false

        when (keyEvent.type) {
            KeyEventType.KeyDown -> {
                // Capture Shift state on KeyDown
                isShiftPressed = keyEvent.isShiftPressed
                true // Consume KeyDown to prevent default behavior
            }
            KeyEventType.KeyUp -> {
                // Use the captured Shift state from KeyDown
                if (isShiftPressed) {
                    focusManager.moveFocus(FocusDirection.Previous)
                } else {
                    focusManager.moveFocus(FocusDirection.Next)
                }
                true
            }
            else -> false
        }
    }
}
