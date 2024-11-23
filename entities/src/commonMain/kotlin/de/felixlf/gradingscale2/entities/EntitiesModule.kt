package de.felixlf.gradingscale2.entities

import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepositoryImpl
import de.felixlf.gradingscale2.entities.util.GradeScaleGenerator
import org.koin.dsl.module

val entitiesModule =
    module {
        single { GradeScaleGenerator() }
        single<GradeScaleRepository> { GradeScaleRepositoryImpl(get()) }
    }
