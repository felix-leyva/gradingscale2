package de.felixlf.gradingscale2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import de.felixlf.gradingscale2.di.mainModule
import de.felixlf.gradingscale2.navigation.AppNavController
import de.felixlf.gradingscale2.navigation.Destinations
import de.felixlf.gradingscale2.navigation.rememberAppNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppCompositionLifecycleTest {

    @OptIn(KoinInternalApi::class)
    @Test
    fun mainModuleDoesNotRegisterCompositionScopedAppNavControllerOrAppStateAsSingletons() {
        val koinApp = koinApplication {
            modules(mainModule)
        }
        try {
            val backStack = NavBackStack<NavKey>(Destinations.GradeScaleList)
            assertNull(
                koinApp.koin.getOrNull<AppNavController> { parametersOf(backStack) },
                "AppNavController should be composition-scoped, not a process-wide Koin singleton",
            )
            val registeredAppStates = koinApp.koin.instanceRegistry.instances.values.filter { factory ->
                factory.beanDefinition.primaryType == AppState::class ||
                    AppState::class in factory.beanDefinition.secondaryTypes
            }
            assertTrue(
                registeredAppStates.isEmpty(),
                "AppState should be composition-scoped, not registered in mainModule",
            )
        } finally {
            koinApp.close()
        }
    }

    @Test
    fun rememberAppNavControllerBindsToFreshNavBackStackOnCompositionRecreation() = runTest {
        val backStack1 = NavBackStack<NavKey>(Destinations.GradeScaleList)
        val backStack2 = NavBackStack<NavKey>(Destinations.GradeScaleList)
        var currentBackStack by mutableStateOf(backStack1)

        val controllers = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            rememberAppNavController(backStack = currentBackStack)
        }

        controllers.test {
            val controller1 = awaitItem()
            assertSame(backStack1, controller1.backStack)

            currentBackStack = backStack2
            runCurrent()
            val controller2 = awaitItem()
            assertNotSame(controller1, controller2)
            assertSame(backStack2, controller2.backStack)

            controller2.navigateTopLevel(Destinations.GradeScaleCalculator)
            assertEquals(
                listOf<NavKey>(Destinations.GradeScaleList, Destinations.GradeScaleCalculator),
                backStack2.toList(),
            )
            assertEquals(listOf<NavKey>(Destinations.GradeScaleList), backStack1.toList())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
