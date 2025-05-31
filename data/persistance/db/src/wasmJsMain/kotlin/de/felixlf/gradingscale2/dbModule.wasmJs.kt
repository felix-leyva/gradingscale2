@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.daosimpl.GradeScaleDaoStoreImpl
import de.felixlf.gradingscale2.daosimpl.GradesDaoStoreImpl
import de.felixlf.gradingscale2.daosimpl.WeightedGradeDaoStoreImpl
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.daos.WeightedGradeDao
import de.felixlf.gradingscale2.storage.PersistentStoreFactory
import de.felixlf.gradingscale2.storage.StorageUtils
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import de.felixlf.gradingscale2.store.GradeScalesStoreData
import de.felixlf.gradingscale2.store.WeightedGradesStore
import de.felixlf.gradingscale2.store.WeightedGradesStoreData
import de.felixlf.gradingscale2.store.WeightedGradesStoreProvider
import kotlinx.collections.immutable.persistentListOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual fun getDbPlatformModule(): Module =
    module {
        // GradeScale store with localStorage persistence
        single<GradeScaleStoreProvider> {
            val gradeScalesStore = PersistentStoreFactory.create(
                default = GradeScalesStoreData(persistentListOf()),
                serializer = GradeScalesStoreData.serializer(),
                storageKey = StorageUtils.Keys.GRADE_SCALES,
            )
            GradeScaleStoreProvider(
                gradeScalesStore = gradeScalesStore,
                dispatcherProvider = get(),
            )
        }

        // WeightedGrades store with localStorage persistence
        single<WeightedGradesStore> {
            val weightedGradesStore = PersistentStoreFactory.create(
                default = WeightedGradesStoreData(persistentListOf()),
                serializer = WeightedGradesStoreData.serializer(),
                storageKey = StorageUtils.Keys.WEIGHTED_GRADES,
            )
            WeightedGradesStoreProvider(
                weightedGradesStore = weightedGradesStore,
                dispatcherProvider = get(),
            )
        }

        // DAO implementations
        singleOf(::GradeScaleDaoStoreImpl).bind<GradeScaleDao>()
        singleOf(::GradesDaoStoreImpl).bind<GradesDao>()
        singleOf(::WeightedGradeDaoStoreImpl).bind<WeightedGradeDao>()
    }
