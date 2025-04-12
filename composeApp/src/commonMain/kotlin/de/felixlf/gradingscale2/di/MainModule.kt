package de.felixlf.gradingscale2.di

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavHostController
import de.felixlf.gradingscale2.dbModule
import de.felixlf.gradingscale2.entities.entitiesModule
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase
import de.felixlf.gradingscale2.features.calculator.CalculatorViewModel
import de.felixlf.gradingscale2.features.import.ImportViewModel
import de.felixlf.gradingscale2.features.list.GradeScaleListViewModel
import de.felixlf.gradingscale2.features.list.upsertgradedialog.UpsertGradeViewModel
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleViewModel
import de.felixlf.gradingscale2.features.weightedgradecalculator.WeightedCalculatorViewModel
import de.felixlf.gradingscale2.features.weightedgradecalculator.dialogs.WeightedGradeDialogViewModel
import de.felixlf.gradingscale2.navigation.AppNavController
import de.felixlf.gradingscale2.navigation.AppNavControllerImpl
import de.felixlf.gradingscale2.network.di.networkModule
import de.felixlf.gradingscale2.usecases.ShowSnackbarUseCaseImpl
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    includes(
        getApplicationModule(),
        authModule,
        dbModule,
        networkModule,
        entitiesModule,
    )

    viewModelOf(::GradeScaleListViewModel)
    viewModelOf(::UpsertGradeViewModel)
    viewModelOf(::CalculatorViewModel)
    viewModelOf(::UpsertGradeScaleViewModel)
    viewModelOf(::ImportViewModel)
    viewModelOf(::WeightedCalculatorViewModel)
    viewModelOf(::WeightedGradeDialogViewModel)

    single<AppNavController> { (controller: NavHostController) -> AppNavControllerImpl(controller) }
    single<ShowSnackbarUseCase> { (snackbarHostState: SnackbarHostState) -> ShowSnackbarUseCaseImpl(snackbarHostState) }
}
