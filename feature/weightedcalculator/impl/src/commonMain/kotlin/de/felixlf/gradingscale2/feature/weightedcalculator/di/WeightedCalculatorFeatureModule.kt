package de.felixlf.gradingscale2.feature.weightedcalculator.di

import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightCalculatorUIModelWithEvents
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIModelWithEvents
import de.felixlf.gradingscale2.feature.weightedcalculator.api.WeightedCalculatorScreenEntry
import de.felixlf.gradingscale2.features.weightedgradecalculator.WeightedCalculatorViewModelWithEvents
import de.felixlf.gradingscale2.features.weightedgradecalculator.WeightedGradeCalculatorScreen
import de.felixlf.gradingscale2.features.weightedgradecalculator.dialogs.WeightedGradeDialogViewModelWithEvents
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.viewModel

val weightedCalculatorFeatureModule = module {
    factory<WeightCalculatorUIModelWithEvents>()
    factory<WeightedGradeDialogUIModelWithEvents>()
    viewModel<WeightedCalculatorViewModelWithEvents>()
    viewModel<WeightedGradeDialogViewModelWithEvents>()
    single<WeightedCalculatorScreenEntry> {
        WeightedCalculatorScreenEntry { modifier ->
            WeightedGradeCalculatorScreen(modifier = modifier)
        }
    }
}
