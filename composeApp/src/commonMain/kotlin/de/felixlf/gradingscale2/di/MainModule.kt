package de.felixlf.gradingscale2.di

import androidx.navigation.NavHostController
import de.felixlf.gradingscale2.dbModule
import de.felixlf.gradingscale2.entities.entitiesModule
import de.felixlf.gradingscale2.entities.features.calculator.CalculatorUIStateFactory
import de.felixlf.gradingscale2.entities.features.list.GradeListUIStateFactory
import de.felixlf.gradingscale2.features.calculator.CalculatorViewModel
import de.felixlf.gradingscale2.features.list.GradeScaleListViewModel
import de.felixlf.gradingscale2.features.list.upsertgradedialog.UpsertGradeViewModel
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleViewModel
import de.felixlf.gradingscale2.navigation.AppNavController
import de.felixlf.gradingscale2.navigation.AppNavControllerImpl
import de.felixlf.gradingscale2.network.di.networkModule
import org.koin.core.module.dsl.factoryOf
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
    factoryOf(::GradeListUIStateFactory)
    viewModelOf(::GradeScaleListViewModel)
    viewModelOf(::UpsertGradeViewModel)
    viewModelOf(::CalculatorViewModel)
    factoryOf(::CalculatorUIStateFactory)
    viewModelOf(::UpsertGradeScaleViewModel)
    single<AppNavController> { (controller: NavHostController) -> AppNavControllerImpl(controller) }
}
