package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.core.presentation.di.presentationModule
import de.felixlf.gradingscale2.core.ui.di.coreUiModule
import de.felixlf.gradingscale2.core.usecases.di.usecasesModule
import de.felixlf.gradingscale2.dbModule
import de.felixlf.gradingscale2.feature.calculator.di.calculatorFeatureModule
import de.felixlf.gradingscale2.feature.gradescales.di.gradeScalesFeatureModule
import de.felixlf.gradingscale2.feature.importer.di.importerFeatureModule
import de.felixlf.gradingscale2.feature.weightedcalculator.di.weightedCalculatorFeatureModule
import de.felixlf.gradingscale2.network.di.networkModule
import de.felixlf.gradingscale2.sharedprefs.preferencesModule
import org.koin.dsl.module

val mainModule = module {
    includes(
        getApplicationModule(),
        authModule,
        dbModule,
        networkModule,
        presentationModule,
        coreUiModule,
        usecasesModule,
        calculatorFeatureModule,
        importerFeatureModule,
        weightedCalculatorFeatureModule,
        gradeScalesFeatureModule,
        preferencesModule,
        diagnosticsModule,
    )
}
