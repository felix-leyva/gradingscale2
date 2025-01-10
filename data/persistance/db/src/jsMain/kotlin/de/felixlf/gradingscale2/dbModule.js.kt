@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.daosimpl.GradeScaleDaoStoreImpl
import de.felixlf.gradingscale2.daosimpl.GradesDaoStoreImpl
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import de.felixlf.gradingscale2.store.GradeScalesStore
import io.github.xxfast.kstore.storage.storeOf
import kotlinx.collections.immutable.persistentListOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual fun getDbPlatformModule(): Module =
    module {
        val gradeScaleStorage: GradeScalesStore = storeOf(
            key = GRADE_SCALES_KEY,
            default = persistentListOf<GradeScale>(),
        )
        single { GradeScaleStoreProvider(gradeScalesStore = gradeScaleStorage) }
        singleOf(::GradeScaleDaoStoreImpl).bind<GradeScaleDao>()
        singleOf(::GradesDaoStoreImpl).bind<GradesDao>()
    }

private const val GRADE_SCALES_KEY = "gradeScales"
