package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class GradeScaleTableTsvFormatterImplTest {

    private val formatter = GradeScaleTableTsvFormatterImpl()

    private val sampleRows = listOf(
        ExamTableRow(
            gradeName = "1",
            minPoints = 90.0,
            maxPoints = 100.0,
            minPercentage = 0.90,
            maxPercentage = 1.00,
        ),
        ExamTableRow(
            gradeName = "2",
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
    fun format_withPercentageAndRange_rendersThreeTsvColumnsWithRange() {
        val options = TableExportOptions(
            includePercentage = true,
            showAsRange = true,
        )

        val result = formatter.format(sampleRows, options, defaultHeaders)
        val lines = result.lines()

        assertEquals(3, lines.size)
        assertEquals("Grade\tPoints\tPercentage", lines[0])
        assertEquals("1\t90 – 100\t90% – 100%", lines[1])
        assertEquals("2\t80 – 90\t80% – 90%", lines[2])
    }

    @Test
    fun format_withoutPercentageAndWithoutRange_rendersTwoTsvColumnsWithGreaterThanOrEqual() {
        val options = TableExportOptions(
            includePercentage = false,
            showAsRange = false,
        )

        val result = formatter.format(sampleRows, options, defaultHeaders)
        val lines = result.lines()

        assertEquals(3, lines.size)
        assertEquals("Grade\tPoints", lines[0])
        assertEquals("1\t≥ 90", lines[1])
        assertEquals("2\t≥ 80", lines[2])
    }

    @Test
    fun format_withPercentageAndWithoutRange_rendersThreeTsvColumnsWithGreaterThanOrEqual() {
        val options = TableExportOptions(
            includePercentage = true,
            showAsRange = false,
        )

        val result = formatter.format(sampleRows, options, defaultHeaders)
        val lines = result.lines()

        assertEquals(3, lines.size)
        assertEquals("Grade\tPoints\tPercentage", lines[0])
        assertEquals("1\t≥ 90\t≥ 90%", lines[1])
        assertEquals("2\t≥ 80\t≥ 80%", lines[2])
    }

    @Test
    fun format_withoutPercentageAndWithRange_rendersTwoTsvColumnsWithRange() {
        val options = TableExportOptions(
            includePercentage = false,
            showAsRange = true,
        )

        val result = formatter.format(sampleRows, options, defaultHeaders)
        val lines = result.lines()

        assertEquals(3, lines.size)
        assertEquals("Grade\tPoints", lines[0])
        assertEquals("1\t90 – 100", lines[1])
        assertEquals("2\t80 – 90", lines[2])
    }

    @Test
    fun format_sanitizesTabsAndNewlines() {
        val rowsWithTabsAndNewlines = listOf(
            ExamTableRow(
                gradeName = "Grade\t1\r\nPlus",
                minPoints = 95.0,
                maxPoints = 100.0,
                minPercentage = 0.95,
                maxPercentage = 1.00,
            ),
        )
        val headersWithTabsAndNewlines = TableHeaders(
            gradeTitle = "Grade\tName",
            pointsTitle = "Points\r\nCol",
            percentageTitle = "Pct\tCol",
        )
        val options = TableExportOptions(includePercentage = true, showAsRange = false)

        val result = formatter.format(rowsWithTabsAndNewlines, options, headersWithTabsAndNewlines)
        val lines = result.lines()

        assertEquals(2, lines.size)
        assertEquals("Grade Name\tPoints  Col\tPct Col", lines[0])
        assertEquals("Grade 1  Plus\t≥ 95\t≥ 95%", lines[1])
        assertFalse(lines[0].startsWith("Grade\tName"))
    }
}
