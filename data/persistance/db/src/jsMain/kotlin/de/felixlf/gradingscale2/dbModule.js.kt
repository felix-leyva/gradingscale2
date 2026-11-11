@file:Suppress("ktlint:standard:filename")
@file:OptIn(ExperimentalSerializationApi::class)

package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.daosimpl.GradeScaleDaoStoreImpl
import de.felixlf.gradingscale2.daosimpl.GradesDaoStoreImpl
import de.felixlf.gradingscale2.daosimpl.WeightedGradeDaoStoreImpl
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.daos.WeightedGradeDao
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import de.felixlf.gradingscale2.store.GradeScalesStoreData
import de.felixlf.gradingscale2.store.WeightedGradesStore
import de.felixlf.gradingscale2.store.WeightedGradesStoreData
import de.felixlf.gradingscale2.store.WeightedGradesStoreProvider
import io.github.xxfast.kstore.storage.storeOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single

private fun provideGradeScaleStoreProvider(dispatcherProvider: DispatcherProvider): GradeScaleStoreProvider {
    val gradeScalesStore = storeOf(
        key = GRADE_SCALES_KEY,
        format = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            isLenient = true
        },
        default = GradeScalesStoreData(persistentListOf()),
    )
    return GradeScaleStoreProvider(gradeScalesStore = gradeScalesStore, dispatcherProvider = dispatcherProvider)
}

private fun provideWeightedGradesStore(dispatcherProvider: DispatcherProvider): WeightedGradesStore {
    val weightedGradesStore = storeOf(
        key = WEIGHTED_GRADES_KEY,
        format = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            isLenient = true
        },
        default = WeightedGradesStoreData(persistentListOf()),
    )
    return WeightedGradesStoreProvider(weightedGradesStore = weightedGradesStore, dispatcherProvider = dispatcherProvider)
}

internal actual fun getDbPlatformModule(): Module =
    module {
        single { create(::provideGradeScaleStoreProvider) }
        single { create(::provideWeightedGradesStore) }
        single<GradeScaleDaoStoreImpl>() bind GradeScaleDao::class
        single<GradesDaoStoreImpl>() bind GradesDao::class
        single<WeightedGradeDaoStoreImpl>() bind WeightedGradeDao::class
    }

private const val GRADE_SCALES_KEY = "gradeScales"
private const val WEIGHTED_GRADES_KEY = "weightedGrades"
