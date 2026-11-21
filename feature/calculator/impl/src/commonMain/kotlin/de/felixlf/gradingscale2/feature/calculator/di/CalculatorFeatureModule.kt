package de.felixlf.gradingscale2.feature.calculator.di

import de.felixlf.gradingscale2.entities.features.calculator.CalculatorUIModel
import de.felixlf.gradingscale2.feature.calculator.api.CalculatorScreenEntry
import de.felixlf.gradingscale2.features.calculator.CalculatorViewModel
import de.felixlf.gradingscale2.features.calculator.GradeScaleCalculatorScreen
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.viewModel

val calculatorFeatureModule = module {
    factory<CalculatorUIModel>()
    viewModel<CalculatorViewModel>()
    single<CalculatorScreenEntry> {
        CalculatorScreenEntry { modifier ->
            GradeScaleCalculatorScreen(modifier = modifier)
        }
    }
}
