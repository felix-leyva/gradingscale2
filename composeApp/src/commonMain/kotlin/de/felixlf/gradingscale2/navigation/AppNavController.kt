package de.felixlf.gradingscale2.navigation

import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Container of the navigation back stack.
 */
interface AppNavController {
    val backStack: NavBackStack<NavKey>

    /** The destination currently on top of the back stack. */
    val current: NavKey?

    /**
     * Switches between top-level destinations. The start destination always stays at the root of the stack, so pressing
     * back from any tab returns to it and a second back press leaves the app.
     */
    fun navigateTopLevel(destination: Destinations)
}

internal class AppNavControllerImpl(override val backStack: NavBackStack<NavKey>) : AppNavController {
    override val current: NavKey? get() = backStack.lastOrNull()

    override fun navigateTopLevel(destination: Destinations) {
        if (current == destination) return
        // Atomic and never empty: NavDisplay observes every intermediate state of the list and crashes on an empty
        // back stack, so the start destination must stay in place while switching.
        Snapshot.withMutableSnapshot {
            backStack.retainAll { it == Destinations.GradeScaleList }
            if (backStack.isEmpty()) {
                backStack.add(Destinations.GradeScaleList)
            }
            if (destination != Destinations.GradeScaleList) {
                backStack.add(destination)
            }
        }
    }
}
