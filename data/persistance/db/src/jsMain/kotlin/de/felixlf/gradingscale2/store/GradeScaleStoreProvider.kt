package de.felixlf.gradingscale2.store

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.shareIn

class GradeScaleStoreProvider(
    val gradeScalesStore: GradeScalesStore,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val coroutineScope = CoroutineScope(dispatcher + CoroutineName("GradeScaleStoreProvider"))

    val flow = gradeScalesStore.updates
        .filterNotNull()
        .shareIn(scope = coroutineScope, started = SharingStarted.WhileSubscribed(), replay = 1)
}
