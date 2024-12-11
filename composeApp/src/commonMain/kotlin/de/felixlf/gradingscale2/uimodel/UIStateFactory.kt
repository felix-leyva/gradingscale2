package de.felixlf.gradingscale2.uimodel

import androidx.compose.runtime.Composable

/**
 * A Factory that produces a UI State of type [T] using a [Composable] function.
 *
 * The dependencies required to produce the UI State should be provided in the constructor of the implementing class.
 */
internal fun interface UIStateFactory<T> {

    /**
     * Produces the UI State of type [T] using a [Composable] function.
     *
     * The UI State should be produced using the provided dependencies in the constructor of the implementing class.
     */
    @Composable
    fun produceUI(): T
}
