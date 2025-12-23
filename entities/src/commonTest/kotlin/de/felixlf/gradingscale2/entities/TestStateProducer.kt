package de.felixlf.gradingscale2.entities

import app.cash.molecule.RecompositionMode
import de.felixlf.gradingscale2.entities.uimodel.StateProducer
import kotlinx.coroutines.CoroutineScope

class TestStateProducer(
    override val scope: CoroutineScope,
) : StateProducer {
    override val recompositionMode: RecompositionMode = RecompositionMode.Immediate
}
