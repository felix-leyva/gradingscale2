package de.felixlf.gradingscale2.di

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.SnackbarHostState
import de.felixlf.gradingscale2.AppState
import de.felixlf.gradingscale2.dbModule
import de.felixlf.gradingscale2.entities.entitiesModule
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase
import de.felixlf.gradingscale2.features.calculator.CalculatorViewModel
import de.felixlf.gradingscale2.features.import.ImportViewModelWithEvents
import de.felixlf.gradingscale2.features.list.GradeScaleListViewModel
import de.felixlf.gradingscale2.features.list.exportdialog.ExportExamTableViewModel
import de.felixlf.gradingscale2.features.list.upsertgradedialog.UpsertGradeViewModel
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleViewModel
import de.felixlf.gradingscale2.features.weightedgradecalculator.WeightedCalculatorViewModelWithEvents
import de.felixlf.gradingscale2.features.weightedgradecalculator.dialogs.WeightedGradeDialogViewModelWithEvents
import de.felixlf.gradingscale2.navigation.AppNavController
import de.felixlf.gradingscale2.navigation.AppNavControllerImpl
import de.felixlf.gradingscale2.network.di.networkModule
import de.felixlf.gradingscale2.sharedprefs.preferencesModule
import de.felixlf.gradingscale2.usecases.ShowSnackbarUseCaseImpl
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel

@OptIn(ExperimentalSharedTransitionApi::class)
val mainModule = module {
    includes(
        getApplicationModule(),
        authModule,
        dbModule,
        networkModule,
        entitiesModule,
        preferencesModule,
        diagnosticsModule,
    )

    viewModel<GradeScaleListViewModel>()
    viewModel<UpsertGradeViewModel>()
    viewModel<CalculatorViewModel>()
    viewModel<UpsertGradeScaleViewModel>()
    viewModel<ImportViewModelWithEvents>()
    viewModel<WeightedCalculatorViewModelWithEvents>()
    viewModel<WeightedGradeDialogViewModelWithEvents>()
    viewModel<ExportExamTableViewModel>()

    single<AppNavControllerImpl>() bind AppNavController::class
    single<SnackbarHostState>()
    single<ShowSnackbarUseCaseImpl>() bind ShowSnackbarUseCase::class
    single<AppState> { (sharedTransitionScope: SharedTransitionScope) ->
        AppState(navController = get(), sharedTransitionScope = sharedTransitionScope)
    }
}
