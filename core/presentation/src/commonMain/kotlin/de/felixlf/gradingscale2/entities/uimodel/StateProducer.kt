package de.felixlf.gradingscale2.entities.uimodel

import androidx.compose.runtime.Composable
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface StateProducer {
    val scope: CoroutineScope
    val recompositionMode: RecompositionMode

    operator fun <UIState> invoke(producer: @Composable () -> UIState): Lazy<StateFlow<UIState>> = lazy {
        scope.launchMolecule(recompositionMode) {
            producer()
        }
    }
}
