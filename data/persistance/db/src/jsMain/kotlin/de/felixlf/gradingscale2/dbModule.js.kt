@file:Suppress("ktlint:standard:filename")
@file:OptIn(ExperimentalSerializationApi::class)

package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.daosimpl.GradeScaleDaoStoreImpl
import de.felixlf.gradingscale2.daosimpl.GradesDaoStoreImpl
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import de.felixlf.gradingscale2.store.GradeScalesStore
import de.felixlf.gradingscale2.store.GradeScalesStoreData
import io.github.xxfast.kstore.storage.storeOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual fun getDbPlatformModule(): Module =
    module {
        single<GradeScalesStore> {
            storeOf(
                key = GRADE_SCALES_KEY,
                default = GradeScalesStoreData(persistentListOf()),
            )
        }
        singleOf(::GradeScaleStoreProvider)
        singleOf(::GradeScaleDaoStoreImpl).bind<GradeScaleDao>()
        singleOf(::GradesDaoStoreImpl).bind<GradesDao>()
    }

private const val GRADE_SCALES_KEY = "gradeScales"
