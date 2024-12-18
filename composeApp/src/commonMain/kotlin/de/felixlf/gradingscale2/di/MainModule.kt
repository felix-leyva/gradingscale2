package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.Initializer
import de.felixlf.gradingscale2.InitializerImpl
import de.felixlf.gradingscale2.dbModule
import de.felixlf.gradingscale2.entities.entitiesModule
import de.felixlf.gradingscale2.features.gradescalecalculator.GradeListUIStateFactory
import de.felixlf.gradingscale2.features.gradescalecalculator.GradeScaleListViewModel
import de.felixlf.gradingscale2.features.gradescalecalculator.editgradedialog.EditGradeViewModel
import de.felixlf.gradingscale2.navigation.AppNavController
import de.felixlf.gradingscale2.navigation.AppNavControllerImpl
import de.felixlf.gradingscale2.network.di.networkModule
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule =
    module {
        includes(
            getApplicationModule(),
            authModule,
            dbModule,
            networkModule,
            entitiesModule,
        )
        singleOf(::InitializerImpl).bind<Initializer>()
        factoryOf(::GradeListUIStateFactory)
        factoryOf(::GradeScaleListViewModel)
        factoryOf(::EditGradeViewModel)
        singleOf(::AppNavControllerImpl).bind<AppNavController>()
    }
