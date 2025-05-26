package de.felixlf.gradingscale2.entities

import de.felixlf.gradingscale2.entities.features.calculator.CalculatorUIModel
import de.felixlf.gradingscale2.entities.features.import.ImportUIModel
import de.felixlf.gradingscale2.entities.features.list.GradeListUIModel
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightCalculatorUIModel
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIModel
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepositoryImpl
import de.felixlf.gradingscale2.entities.repositories.GradesRepository
import de.felixlf.gradingscale2.entities.repositories.GradesRepositoryImpl
import de.felixlf.gradingscale2.entities.repositories.PreferencesRepository
import de.felixlf.gradingscale2.entities.repositories.PreferencesRepositoryImpl
import de.felixlf.gradingscale2.entities.repositories.RemoteSyncRepository
import de.felixlf.gradingscale2.entities.repositories.RemoteSyncRepositoryImpl
import de.felixlf.gradingscale2.entities.repositories.WeightedGradesRepository
import de.felixlf.gradingscale2.entities.repositories.WeightedGradesRepositoryImpl
import de.felixlf.gradingscale2.entities.uimodel.UIModelScope
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeScaleUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.DeleteWeightedGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.DeleteWeightedGradeUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetAllWeightedGradesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllWeightedGradesUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetRemoteGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.GetRemoteGradeScaleUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.GetRemoteGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetRemoteGradeScalesUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.ImportRemoteGradeScaleIntoDbUseCase
import de.felixlf.gradingscale2.entities.usecases.ImportRemoteGradeScaleIntoDbUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.InsertGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeScaleUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.InsertGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.UpdateGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.UpdateGradeScaleUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.UpsertWeightedGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertWeightedGradeUseCaseImpl
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import de.felixlf.gradingscale2.entities.util.GradeScaleGenerator
import de.felixlf.gradingscale2.entities.util.ResourceGradeScaleGenerator
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val entitiesModule =
    module {
        singleOf(::ResourceGradeScaleGenerator).bind<GradeScaleGenerator>()
        single<GradeScaleRepository> { GradeScaleRepositoryImpl(get()) }
        single<GradesRepository> { GradesRepositoryImpl(get()) }
        single<RemoteSyncRepository> { RemoteSyncRepositoryImpl(get()) }
        singleOf(::WeightedGradesRepositoryImpl).bind<WeightedGradesRepository>()
        singleOf(::PreferencesRepositoryImpl).bind<PreferencesRepository>()

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
        singleOf(::GetRemoteGradeScaleUseCaseImpl).bind<GetRemoteGradeScaleUseCase>()
        singleOf(::GetRemoteGradeScalesUseCaseImpl).bind<GetRemoteGradeScalesUseCase>()
        singleOf(::ImportRemoteGradeScaleIntoDbUseCaseImpl).bind<ImportRemoteGradeScaleIntoDbUseCase>()
        singleOf(::GetAllWeightedGradesUseCaseImpl).bind<GetAllWeightedGradesUseCase>()
        singleOf(::DeleteWeightedGradeUseCaseImpl).bind<DeleteWeightedGradeUseCase>()
        singleOf(::UpsertWeightedGradeUseCaseImpl).bind<UpsertWeightedGradeUseCase>()
        singleOf(::GetLastSelectedGradeScaleIdUseCaseImpl).bind<GetLastSelectedGradeScaleIdUseCase>()
        singleOf(::SetLastSelectedGradeScaleIdUseCaseImpl).bind<SetLastSelectedGradeScaleIdUseCase>()

        // UI Model
        factory { get<DispatcherProvider>().newUIScope() }.bind<UIModelScope>()
        factoryOf(::GradeListUIModel)
        factoryOf(::CalculatorUIModel)
        factoryOf(::ImportUIModel)
        factoryOf(::WeightCalculatorUIModel)
        factoryOf(::WeightedGradeDialogUIModel)
    }
