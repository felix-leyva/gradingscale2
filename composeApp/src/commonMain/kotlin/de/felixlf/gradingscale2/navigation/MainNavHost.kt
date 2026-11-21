package de.felixlf.gradingscale2.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import de.felixlf.gradingscale2.feature.calculator.api.CalculatorScreenEntry
import de.felixlf.gradingscale2.feature.gradescales.api.GradeScalesScreenEntry
import de.felixlf.gradingscale2.feature.importer.api.ImporterScreenEntry
import de.felixlf.gradingscale2.feature.weightedcalculator.api.WeightedCalculatorScreenEntry
import de.felixlf.gradingscale2.scaffold.PersistentScaffold
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject

/**
 * Serialization setup used to persist the back stack. Non-JVM targets (iOS, wasm) have no reflection, so every
 * [NavKey] implementation must be registered explicitly.
 */
internal val navSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Destinations::class)
        }
    }
}

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    appNavController: AppNavController,
) {
    NavDisplay(
        backStack = appNavController.backStack,
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Destinations> { destination ->
                when (destination) {
                    Destinations.GradeScaleList -> {
                        val gradeScalesEntry = koinInject<GradeScalesScreenEntry>()
                        PersistentScaffold { gradeScalesEntry(Modifier.padding(it)) }
                    }

                    Destinations.GradeScaleCalculator -> {
                        val calculatorEntry = koinInject<CalculatorScreenEntry>()
                        PersistentScaffold { calculatorEntry(Modifier.padding(it)) }
                    }

                    Destinations.WeightedGradeCalculator -> {
                        val weightedCalculatorEntry = koinInject<WeightedCalculatorScreenEntry>()
                        PersistentScaffold { weightedCalculatorEntry(Modifier.padding(it)) }
                    }

                    Destinations.GradeImporter -> {
                        val importerEntry = koinInject<ImporterScreenEntry>()
                        PersistentScaffold { importerEntry(Modifier.padding(it)) }
                    }
                }
            }
        },
    )
}
