@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.daosimpl.GradeScaleDaoStoreImpl
import de.felixlf.gradingscale2.daosimpl.GradesDaoStoreImpl
import de.felixlf.gradingscale2.daosimpl.WeightedGradeDaoStoreImpl
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.daos.WeightedGradeDao
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import de.felixlf.gradingscale2.storage.PersistentStoreFactory
import de.felixlf.gradingscale2.storage.StorageUtils
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import de.felixlf.gradingscale2.store.GradeScalesStoreData
import de.felixlf.gradingscale2.store.WeightedGradesStore
import de.felixlf.gradingscale2.store.WeightedGradesStoreData
import de.felixlf.gradingscale2.store.WeightedGradesStoreProvider
import kotlinx.collections.immutable.persistentListOf
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single

private fun provideGradeScaleStoreProvider(dispatcherProvider: DispatcherProvider): GradeScaleStoreProvider {
    val gradeScalesStore = PersistentStoreFactory.create(
        default = GradeScalesStoreData(persistentListOf()),
        serializer = GradeScalesStoreData.serializer(),
        storageKey = StorageUtils.Keys.GRADE_SCALES,
    )
    return GradeScaleStoreProvider(
        gradeScalesStore = gradeScalesStore,
        dispatcherProvider = dispatcherProvider,
    )
}

private fun provideWeightedGradesStore(dispatcherProvider: DispatcherProvider): WeightedGradesStore {
    val weightedGradesStore = PersistentStoreFactory.create(
        default = WeightedGradesStoreData(persistentListOf()),
        serializer = WeightedGradesStoreData.serializer(),
        storageKey = StorageUtils.Keys.WEIGHTED_GRADES,
    )
    return WeightedGradesStoreProvider(
        weightedGradesStore = weightedGradesStore,
        dispatcherProvider = dispatcherProvider,
    )
}

internal actual fun getDbPlatformModule(): Module =
    module {
        single { create(::provideGradeScaleStoreProvider) }
        single { create(::provideWeightedGradesStore) }

        // DAO implementations
        single<GradeScaleDaoStoreImpl>() bind GradeScaleDao::class
        single<GradesDaoStoreImpl>() bind GradesDao::class
        single<WeightedGradeDaoStoreImpl>() bind WeightedGradeDao::class
    }
