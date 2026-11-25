package de.felixlf.gradingscale2.entities.features.list

import app.cash.turbine.test
import arrow.core.Some
import de.felixlf.gradingscale2.entities.TestStateProducer
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.uimodel.StateProducer
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.single
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/**
 * Regression test for A7-01: the Koin graph declared exactly as in production
 * (`factory<GradeListUIModel>()`) must actually inject DeleteGradeScaleUseCase.
 *
 * If the Koin compiler plugin silently omits the nullable constructor parameter
 * (`deleteGradeScaleUseCase: DeleteGradeScaleUseCase? = null`), deleting a grade
 * scale becomes a silent no-op instead of an error, which this test catches.
 *
 * The use cases are bound through their public fun interfaces: the concrete
 * *UseCaseImpl classes are internal to :core:usecases:impl and their wiring to the
 * repositories is covered by that module's own tests. What is under test here is
 * the Koin -> UIModel boundary.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GradeListUIModelKoinInjectionTest {

    private val scale1 = GradeScale(
        id = "scale-1",
        gradeScaleName = "Standard Scale",
        totalPoints = 100.0,
        grades = persistentListOf(
            Grade(namedGrade = "1", percentage = 0.9, idOfGradeScale = "scale-1", nameOfScale = "Standard Scale", uuid = "u1"),
        ),
    )

    private val scale2 = GradeScale(
        id = "scale-2",
        gradeScaleName = "Gymnasium Scale",
        totalPoints = 50.0,
        grades = persistentListOf(
            Grade(namedGrade = "Sehr Gut", percentage = 0.95, idOfGradeScale = "scale-2", nameOfScale = "Gymnasium Scale", uuid = "u3"),
        ),
    )

    @Test
    fun koinFactoryInjectsDeleteGradeScaleUseCase() = runTest {
        val scalesFlow = MutableStateFlow(listOf(scale1, scale2))
        var lastSavedSelectedId: String? = "scale-1"

        val testModule = module {
            single<StateProducer> { TestStateProducer(backgroundScope) }
            single<GetAllGradeScalesUseCase> {
                GetAllGradeScalesUseCase { scalesFlow.map { it.toImmutableList() } }
            }
            single<GetGradeScaleByIdUseCase> {
                GetGradeScaleByIdUseCase { id -> scalesFlow.map { list -> list.firstOrNull { it.id == id } } }
            }
            single<GetLastSelectedGradeScaleIdUseCase> {
                GetLastSelectedGradeScaleIdUseCase { lastSavedSelectedId }
            }
            single<SetLastSelectedGradeScaleIdUseCase> {
                SetLastSelectedGradeScaleIdUseCase { id ->
                    lastSavedSelectedId = id
                    Some(Unit)
                }
            }
            single<DeleteGradeScaleUseCase> {
                DeleteGradeScaleUseCase { id ->
                    scalesFlow.value = scalesFlow.value.filterNot { it.id == id }
                    Some(Unit)
                }
            }
            // Declared exactly as in the production feature module, so the compiler
            // plugin has to honour GradeListUIModel's nullable DeleteGradeScaleUseCase parameter.
            factory<GradeListUIModel>()
        }

        val koin = koinApplication { modules(testModule) }.koin
        val sut = koin.get<GradeListUIModel>()

        // Bounded timeout: a missing injection must fail loudly instead of
        // hanging the test on an awaitItem() that never completes.
        sut.uiState.test(timeout = 5.seconds) {
            var current = awaitItem()
            while (current.selectedGradeScale?.id != "scale-1") {
                current = awaitItem()
            }

            sut.sendCommand(GradeScaleListUIEvent.DeleteGradeScale("scale-1"))
            runCurrent()

            assertEquals(
                listOf("scale-2"),
                scalesFlow.value.map { it.id },
                "DeleteGradeScaleUseCase was not injected: delete was a silent no-op",
            )
            assertEquals("scale-2", sut.uiState.value.selectedGradeScale?.id)
            assertEquals("scale-2", lastSavedSelectedId)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
