package de.felixlf.gradingscale2.entities.features.export

import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.TestStateProducer
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.TableHeaders
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_export_error_copying
import gradingscale2.entities.generated.resources.gradescale_export_success_table_copied
import gradingscale2.entities.generated.resources.gradescale_export_success_text_copied
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.StringResource
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExportExamTableUIModelTest {

    private val testScale = GradeScale(
        id = "scale-1",
        gradeScaleName = "Physics Test",
        totalPoints = 30.0,
        grades = persistentListOf(
            Grade(namedGrade = "1", percentage = 0.90, idOfGradeScale = "scale-1", nameOfScale = "Physics Test", uuid = "g1"),
            Grade(namedGrade = "2", percentage = 0.75, idOfGradeScale = "scale-1", nameOfScale = "Physics Test", uuid = "g2"),
            Grade(namedGrade = "6", percentage = 0.00, idOfGradeScale = "scale-1", nameOfScale = "Physics Test", uuid = "g3"),
        ),
    )

    private var lastCopiedHtml: String? = null
    private var lastCopiedText: String? = null
    private var copyResult: Boolean = true

    private val mockClipboardExporter = object : TableClipboardExporter {
        override suspend fun copyTableToClipboard(html: String, plainText: String): Boolean {
            lastCopiedHtml = html
            lastCopiedText = plainText
            return copyResult
        }
    }

    private var lastSnackbarMessage: StringResource? = null

    private val mockShowSnackbarUseCase = ShowSnackbarUseCase { message, _, _ ->
        lastSnackbarMessage = message
        ShowSnackbarUseCase.SnackbarResult.ActionPerformed
    }

    private fun TestScope.createUIModel(
        clipboardExporter: TableClipboardExporter = mockClipboardExporter,
        showSnackbar: ShowSnackbarUseCase = mockShowSnackbarUseCase,
    ): ExportExamTableUIModel {
        return ExportExamTableUIModel(
            stateProducer = TestStateProducer(backgroundScope),
            examTableDataBuilder = ExamTableDataBuilderImpl(),
            htmlFormatter = GradeScaleTableHtmlFormatterImpl(),
            tsvFormatter = GradeScaleTableTsvFormatterImpl(),
            tableClipboardExporter = clipboardExporter,
            showSnackbarUseCase = showSnackbar,
        )
    }

    @BeforeTest
    fun setup() {
        lastCopiedHtml = null
        lastCopiedText = null
        copyResult = true
        lastSnackbarMessage = null
    }

    @Test
    fun state_updatesWhenGradeScaleAndPointsSet() = runTest {
        val model = createUIModel()
        model.uiState.test {
            val initial = awaitItem()
            assertFalse(initial.hasScale)

            model.sendCommand(ExportExamTableUIEvent.SetGradeScale(testScale))
            model.sendCommand(ExportExamTableUIEvent.SetTotalPoints(50.0))

            val updated = awaitItem()
            assertTrue(updated.hasScale)
            assertEquals("Physics Test", updated.scaleName)
            assertEquals(50.0, updated.totalPoints)
            assertEquals(3, updated.rows.size)
            assertEquals("Physics Test • 50 pts", updated.subtitleText)
        }
    }

    @Test
    fun state_togglesOptionsCorrectly() = runTest {
        val model = createUIModel()
        model.sendCommand(ExportExamTableUIEvent.SetGradeScale(testScale))

        model.uiState.test {
            awaitItem()

            model.sendCommand(ExportExamTableUIEvent.ToggleIncludePercentage)
            val noPercentage = awaitItem()
            assertFalse(noPercentage.includePercentage)

            model.sendCommand(ExportExamTableUIEvent.ToggleShowAsRange)
            val withRange = awaitItem()
            assertTrue(withRange.showAsRange)

            model.sendCommand(ExportExamTableUIEvent.ToggleSortDescending)
            val ascending = awaitItem()
            assertFalse(ascending.sortDescending)
            assertEquals("6", ascending.rows.first().gradeName)
        }
    }

    @Test
    fun copyWordTable_callsExporterAndShowsSuccessSnackbar() = runTest {
        val model = createUIModel()
        model.sendCommand(ExportExamTableUIEvent.SetGradeScale(testScale))
        model.sendCommand(ExportExamTableUIEvent.SetTotalPoints(30.0))
        model.sendCommand(ExportExamTableUIEvent.SetHeaders(TableHeaders("Nota", "Puntos", "%")))

        model.uiState.test {
            awaitItem()

            model.sendCommand(ExportExamTableUIEvent.CopyWordTable)
            testScheduler.advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state.isCopiedDismiss)
            assertTrue(lastCopiedHtml?.contains("<table") == true)
            assertTrue(lastCopiedHtml?.contains("Nota") == true)
            assertEquals(Res.string.gradescale_export_success_table_copied, lastSnackbarMessage)
        }
    }

    @Test
    fun copyWordTable_handlesFailureGracefully() = runTest {
        copyResult = false
        val model = createUIModel()
        model.sendCommand(ExportExamTableUIEvent.SetGradeScale(testScale))

        model.sendCommand(ExportExamTableUIEvent.CopyWordTable)
        testScheduler.advanceUntilIdle()
        kotlinx.coroutines.yield()
        testScheduler.advanceUntilIdle()

        assertEquals(Res.string.gradescale_export_error_copying, lastSnackbarMessage)
    }

    @Test
    fun copyTsvTable_callsExporterAndShowsSuccessSnackbar() = runTest {
        val model = createUIModel()
        model.sendCommand(ExportExamTableUIEvent.SetGradeScale(testScale))
        model.sendCommand(ExportExamTableUIEvent.SetTotalPoints(30.0))

        model.uiState.test {
            awaitItem()

            model.sendCommand(ExportExamTableUIEvent.CopyTsvTable)
            testScheduler.advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state.isCopiedDismiss)
            assertEquals("", lastCopiedHtml)
            assertTrue(lastCopiedText?.contains("Grade\tPoints") == true)
            assertEquals(Res.string.gradescale_export_success_text_copied, lastSnackbarMessage)
        }
    }

    @Test
    fun dismissHandled_resetsCopiedDismissFlag() = runTest {
        val model = createUIModel()
        model.sendCommand(ExportExamTableUIEvent.SetGradeScale(testScale))

        model.uiState.test {
            awaitItem()

            model.sendCommand(ExportExamTableUIEvent.CopyWordTable)
            testScheduler.advanceUntilIdle()
            val stateAfterCopy = awaitItem()
            assertTrue(stateAfterCopy.isCopiedDismiss)

            model.sendCommand(ExportExamTableUIEvent.DismissHandled)
            val stateAfterReset = awaitItem()
            assertFalse(stateAfterReset.isCopiedDismiss)
        }
    }
}
