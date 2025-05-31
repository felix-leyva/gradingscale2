package de.felixlf.gradingscale2.store

import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class GradeScaleStoreProvider(
    val gradeScalesStore: GradeScalesStore,
    dispatcherProvider: DispatcherProvider,
) {
    private val coroutineScope = CoroutineScope(dispatcherProvider.io + CoroutineName("GradeScaleStoreProvider"))

    val flow = gradeScalesStore.updates
        .map { it?.gradeScales ?: persistentListOf() }
        .shareIn(scope = coroutineScope, started = SharingStarted.WhileSubscribed(), replay = 1)
}
