package de.felixlf.gradingscale2.entities

import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepositoryImpl
import de.felixlf.gradingscale2.entities.repositories.GradesRepository
import de.felixlf.gradingscale2.entities.repositories.GradesRepositoryImpl
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeScaleUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeScaleUseCaseImpl
import de.felixlf.gradingscale2.entities.util.GradeScaleGenerator
import org.koin.dsl.module

val entitiesModule =
    module {
        single { GradeScaleGenerator() }
        single<GradeScaleRepository> { GradeScaleRepositoryImpl(get()) }
        single<GradesRepository> { GradesRepositoryImpl(get()) }

        // Use cases
        single<GetAllGradeScalesUseCase> { GetAllGradeScalesUseCaseImpl(get()) }
        single<GetGradeScaleByIdUseCase> { GetGradeScaleByIdUseCaseImpl(get()) }
        single<UpsertGradeScaleUseCase> { UpsertGradeScaleUseCaseImpl() }
        single<DeleteGradeScaleUseCase> { DeleteGradeScaleUseCaseImpl() }
    }
