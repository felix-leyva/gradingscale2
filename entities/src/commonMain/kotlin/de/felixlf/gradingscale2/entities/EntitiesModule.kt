package de.felixlf.gradingscale2.entities

import de.felixlf.gradingscale2.entities.features.calculator.CalculatorUIModel
import de.felixlf.gradingscale2.entities.features.export.ExamTableDataBuilder
import de.felixlf.gradingscale2.entities.features.export.ExamTableDataBuilderImpl
import de.felixlf.gradingscale2.entities.features.export.ExportExamTableUIModel
import de.felixlf.gradingscale2.entities.features.export.GradeScaleTableHtmlFormatter
import de.felixlf.gradingscale2.entities.features.export.GradeScaleTableHtmlFormatterImpl
import de.felixlf.gradingscale2.entities.features.export.GradeScaleTableMarkdownFormatter
import de.felixlf.gradingscale2.entities.features.export.GradeScaleTableMarkdownFormatterImpl
import de.felixlf.gradingscale2.entities.features.export.GradeScaleTableTsvFormatter
import de.felixlf.gradingscale2.entities.features.export.GradeScaleTableTsvFormatterImpl
import de.felixlf.gradingscale2.entities.features.import.ImportUIModelWithEvents
import de.felixlf.gradingscale2.entities.features.list.GradeListUIModel
import de.felixlf.gradingscale2.entities.features.list.upsertgradedialog.UpsertGradeUIFactory
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIStateFactory
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightCalculatorUIModelWithEvents
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIModelWithEvents
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
import de.felixlf.gradingscale2.entities.usecases.TrackErrorUseCase
import de.felixlf.gradingscale2.entities.usecases.TrackErrorUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.UpdateGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.UpdateGradeScaleUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCaseImpl
import de.felixlf.gradingscale2.entities.usecases.UpsertWeightedGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertWeightedGradeUseCaseImpl
import de.felixlf.gradingscale2.entities.util.GradeScaleGenerator
import de.felixlf.gradingscale2.entities.util.ResourceGradeScaleGenerator
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.single

expect val platformEntitiesModule: Module

val entitiesModule =
    module {
        includes(platformEntitiesModule)
        single<ResourceGradeScaleGenerator>() bind GradeScaleGenerator::class
        single<GradeScaleRepositoryImpl>() bind GradeScaleRepository::class
        single<GradesRepositoryImpl>() bind GradesRepository::class
        single<RemoteSyncRepositoryImpl>() bind RemoteSyncRepository::class
        single<WeightedGradesRepositoryImpl>() bind WeightedGradesRepository::class
        single<PreferencesRepositoryImpl>() bind PreferencesRepository::class

        // Use cases
        single<GetAllGradeScalesUseCaseImpl>() bind GetAllGradeScalesUseCase::class
        single<GetGradeScaleByIdUseCaseImpl>() bind GetGradeScaleByIdUseCase::class
        single<InsertGradeScaleUseCaseImpl>() bind InsertGradeScaleUseCase::class
        single<UpdateGradeScaleUseCaseImpl>() bind UpdateGradeScaleUseCase::class
        single<DeleteGradeScaleUseCaseImpl>() bind DeleteGradeScaleUseCase::class
        single<DeleteGradeUseCaseImpl>() bind DeleteGradeUseCase::class
        single<UpsertGradeUseCaseImpl>() bind UpsertGradeUseCase::class
        single<GetGradeByUUIDUseCaseImpl>() bind GetGradeByUUIDUseCase::class
        single<InsertGradeUseCaseImpl>() bind InsertGradeUseCase::class
        single<GetRemoteGradeScaleUseCaseImpl>() bind GetRemoteGradeScaleUseCase::class
        single<GetRemoteGradeScalesUseCaseImpl>() bind GetRemoteGradeScalesUseCase::class
        single<ImportRemoteGradeScaleIntoDbUseCaseImpl>() bind ImportRemoteGradeScaleIntoDbUseCase::class
        single<GetAllWeightedGradesUseCaseImpl>() bind GetAllWeightedGradesUseCase::class
        single<DeleteWeightedGradeUseCaseImpl>() bind DeleteWeightedGradeUseCase::class
        single<UpsertWeightedGradeUseCaseImpl>() bind UpsertWeightedGradeUseCase::class
        single<GetLastSelectedGradeScaleIdUseCaseImpl>() bind GetLastSelectedGradeScaleIdUseCase::class
        single<SetLastSelectedGradeScaleIdUseCaseImpl>() bind SetLastSelectedGradeScaleIdUseCase::class
        single<TrackErrorUseCaseImpl>() bind TrackErrorUseCase::class

        single<ExamTableDataBuilderImpl>() bind ExamTableDataBuilder::class
        single<GradeScaleTableHtmlFormatterImpl>() bind GradeScaleTableHtmlFormatter::class
        single<GradeScaleTableTsvFormatterImpl>() bind GradeScaleTableTsvFormatter::class
        single<GradeScaleTableMarkdownFormatterImpl>() bind GradeScaleTableMarkdownFormatter::class

        // UI Model
        factory<GradeListUIModel>()
        factory<CalculatorUIModel>()
        factory<ImportUIModelWithEvents>()
        factory<UpsertGradeUIFactory>()
        factory<UpsertGradeScaleUIStateFactory>()
        factory<WeightCalculatorUIModelWithEvents>()
        factory<WeightedGradeDialogUIModelWithEvents>()
        factory<ExportExamTableUIModel>()
    }
