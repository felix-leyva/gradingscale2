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

    @Test
    fun format_neutralizesFormulaInjectionPrefixesOnGradesAndHeaders() {
        val maliciousRows = listOf(
            ExamTableRow("=HYPERLINK(\"https://evil.example\",\"A\")", 90.0, 100.0, 0.9, 1.0),
            ExamTableRow("+cmd", 80.0, 90.0, 0.8, 0.9),
            ExamTableRow("-1", 70.0, 80.0, 0.7, 0.8),
            ExamTableRow("@SUM(A1:A2)", 60.0, 70.0, 0.6, 0.7),
            ExamTableRow("  =1+1", 50.0, 60.0, 0.5, 0.6),
            ExamTableRow("A+", 40.0, 50.0, 0.4, 0.5),
            ExamTableRow("B-", 0.0, 40.0, 0.0, 0.4),
        )
        val headers = TableHeaders(
            gradeTitle = "=Header",
            pointsTitle = "Points",
            percentageTitle = "@Pct",
        )
        val result = formatter.format(
            rows = maliciousRows,
            options = TableExportOptions(includePercentage = true, showAsRange = false),
            headers = headers,
        )
        val lines = result.lines()

        assertEquals("'=Header\tPoints\t'@Pct", lines[0])
        assertEquals("'=HYPERLINK(\"https://evil.example\",\"A\")", lines[1].substringBefore('\t'))
        assertEquals("'+cmd", lines[2].substringBefore('\t'))
        assertEquals("'-1", lines[3].substringBefore('\t'))
        assertEquals("'@SUM(A1:A2)", lines[4].substringBefore('\t'))
        assertEquals("'  =1+1", lines[5].substringBefore('\t'))
        assertEquals("A+", lines[6].substringBefore('\t'))
        assertEquals("B-", lines[7].substringBefore('\t'))
    }
}
