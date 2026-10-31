package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GradeScaleTableMarkdownFormatterImplTest {

    private val formatter = GradeScaleTableMarkdownFormatterImpl()

    private val sampleRows = listOf(
        ExamTableRow(
            gradeName = "A",
            minPoints = 90.0,
            maxPoints = 100.0,
            minPercentage = 0.90,
            maxPercentage = 1.00,
        ),
        ExamTableRow(
            gradeName = "B",
            minPoints = 80.0,
            maxPoints = 90.0,
            minPercentage = 0.80,
            maxPercentage = 0.90,
        ),
    )

    private val defaultHeaders = TableHeaders(
        gradeTitle = "Grade",
        pointsTitle = "Points",
        percentageTitle = "Percentage",
    )

    @Test
    fun emptyRows_returnsEmptyString() {
        val result = formatter.format(
            rows = emptyList(),
            options = TableExportOptions(),
            headers = defaultHeaders,
        )
        assertEquals("", result)
    }

    @Test
    fun format_withPercentageAndRange_rendersFullMarkdownTable() {
        val options = TableExportOptions(
            includePercentage = true,
            showAsRange = true,
        )

        val result = formatter.format(sampleRows, options, defaultHeaders)

        val lines = result.lines()
        assertEquals(4, lines.size)
        // Header
        assertEquals("| Grade | Points | Percentage |", lines[0])
        // Separator
        assertEquals("| :---: | :---: | :---: |", lines[1])
        // Rows with range
        assertEquals("| A | 90 – 100 | 90% – 100% |", lines[2])
        assertEquals("| B | 80 – 90 | 80% – 90% |", lines[3])
    }

    @Test
    fun format_withoutPercentageAndWithoutRange_rendersTwoColumnsWithGreaterThanOrEqual() {
        val options = TableExportOptions(
            includePercentage = false,
            showAsRange = false,
        )

        val result = formatter.format(sampleRows, options, defaultHeaders)

        val lines = result.lines()
        assertEquals(4, lines.size)
        // Header without Percentage
        assertEquals("| Grade | Points |", lines[0])
        // Separator with 2 columns
        assertEquals("| :---: | :---: |", lines[1])
        // Rows with ≥ prefix
        assertEquals("| A | ≥ 90 |", lines[2])
        assertEquals("| B | ≥ 80 |", lines[3])
    }

    @Test
    fun format_withPercentageAndWithoutRange_rendersThreeColumnsWithGreaterThanOrEqual() {
        val options = TableExportOptions(
            includePercentage = true,
            showAsRange = false,
        )

        val result = formatter.format(sampleRows, options, defaultHeaders)

        val lines = result.lines()
        assertEquals(4, lines.size)
        assertEquals("| Grade | Points | Percentage |", lines[0])
        assertEquals("| :---: | :---: | :---: |", lines[1])
        assertEquals("| A | ≥ 90 | ≥ 90% |", lines[2])
        assertEquals("| B | ≥ 80 | ≥ 80% |", lines[3])
    }

    @Test
    fun format_withoutPercentageAndWithRange_rendersTwoColumnsWithRange() {
        val options = TableExportOptions(
            includePercentage = false,
            showAsRange = true,
        )

        val result = formatter.format(sampleRows, options, defaultHeaders)

        val lines = result.lines()
        assertEquals(4, lines.size)
        assertEquals("| Grade | Points |", lines[0])
        assertEquals("| :---: | :---: |", lines[1])
        assertEquals("| A | 90 – 100 |", lines[2])
        assertEquals("| B | 80 – 90 |", lines[3])
    }

    @Test
    fun format_escapesMarkdownSpecialCharacters() {
        val specialRows = listOf(
            ExamTableRow(
                gradeName = "A|B\\C\r\nD",
                minPoints = 90.0,
                maxPoints = 100.0,
                minPercentage = 0.90,
                maxPercentage = 1.00,
            ),
        )
        val specialHeaders = TableHeaders(
            gradeTitle = "Grade|Col",
            pointsTitle = "Pts\\Col",
            percentageTitle = "Pct\nCol",
        )
        val options = TableExportOptions(includePercentage = true, showAsRange = true)

        val result = formatter.format(specialRows, options, specialHeaders)

        assertTrue(result.contains("Grade\\|Col"))
        assertTrue(result.contains("Pts\\\\Col"))
        assertTrue(result.contains("Pct Col"))
        assertTrue(result.contains("A\\|B\\\\C  D"))
    }
}
