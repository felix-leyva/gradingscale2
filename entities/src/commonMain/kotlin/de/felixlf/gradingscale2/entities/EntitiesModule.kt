package de.felixlf.gradingscale2.entities

import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepositoryImpl
import de.felixlf.gradingscale2.entities.repositories.GradesRepository
import de.felixlf.gradingscale2.entities.repositories.GradesRepositoryImpl
import de.felixlf.gradingscale2.entities.repositories.RemoteSyncRepository
import de.felixlf.gradingscale2.entities.repositories.RemoteSyncRepositoryImpl
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeScaleUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.InsertGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeScaleUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.InsertGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.UpdateGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.UpdateGradeScaleUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCaseImpl
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import org.koin.dsl.module

val entitiesModule =
    module {
        single { MockGradeScalesGenerator() }
        single<GradeScaleRepository> { GradeScaleRepositoryImpl(get()) }
        single<GradesRepository> { GradesRepositoryImpl(get()) }
        single<RemoteSyncRepository> { RemoteSyncRepositoryImpl(get()) }

        // Use cases
        single<GetAllGradeScalesUseCase> { GetAllGradeScalesUseCaseImpl(get(), get()) }
        single<GetGradeScaleByIdUseCase> { GetGradeScaleByIdUseCaseImpl(get()) }
        single<InsertGradeScaleUseCase> { InsertGradeScaleUseCaseImpl(get()) }
        single<UpdateGradeScaleUseCase> { UpdateGradeScaleUseCaseImpl(get()) }
        single<DeleteGradeScaleUseCase> { DeleteGradeScaleUseCaseImpl(get()) }
        single<DeleteGradeUseCase> { DeleteGradeUseCaseImpl(get()) }
        single<UpsertGradeUseCase> { UpsertGradeUseCaseImpl(get()) }
        single<GetGradeByUUIDUseCase> { GetGradeByUUIDUseCaseImpl(get()) }
        single<InsertGradeUseCase> { InsertGradeUseCaseImpl(get(), get()) }
    }
