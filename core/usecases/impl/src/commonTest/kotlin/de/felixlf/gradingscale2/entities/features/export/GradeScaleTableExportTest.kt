package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GradeScaleTableExportTest {

    private val builder: ExamTableDataBuilder = ExamTableDataBuilderImpl()
    private val htmlFormatter: GradeScaleTableHtmlFormatter = GradeScaleTableHtmlFormatterImpl()
    private val tsvFormatter: GradeScaleTableTsvFormatter = GradeScaleTableTsvFormatterImpl()
    private val mdFormatter: GradeScaleTableMarkdownFormatter = GradeScaleTableMarkdownFormatterImpl()

    private val testGradeScale = GradeScale(
        id = "1",
        gradeScaleName = "Test Scale",
        totalPoints = 25.0,
        grades = persistentListOf(
            Grade(namedGrade = "A", percentage = 0.90, idOfGradeScale = "1", nameOfScale = "Test Scale", uuid = "g1"),
            Grade(namedGrade = "B", percentage = 0.80, idOfGradeScale = "1", nameOfScale = "Test Scale", uuid = "g2"),
            Grade(namedGrade = "C", percentage = 0.70, idOfGradeScale = "1", nameOfScale = "Test Scale", uuid = "g3"),
            Grade(namedGrade = "F", percentage = 0.00, idOfGradeScale = "1", nameOfScale = "Test Scale", uuid = "g4"),
        ),
    )

    @Test
    fun buildRows_calculatesPointsAndRangesCorrectly_descending() {
        val options = TableExportOptions(sortDescending = true)
        val rows = builder.buildRows(testGradeScale, options, totalPoints = 25.0)

        assertEquals(4, rows.size)

        // Grade A
        assertEquals("A", rows[0].gradeName)
        assertEquals(22.5, rows[0].minPoints)
        assertEquals(25.0, rows[0].maxPoints)
        assertEquals(0.90, rows[0].minPercentage)
        assertEquals(1.00, rows[0].maxPercentage)

        // Grade B
        assertEquals("B", rows[1].gradeName)
        assertEquals(20.0, rows[1].minPoints)
        assertEquals(22.5, rows[1].maxPoints)

        // Grade C
        assertEquals("C", rows[2].gradeName)
        assertEquals(17.5, rows[2].minPoints)
        assertEquals(20.0, rows[2].maxPoints)

        // Grade F
        assertEquals("F", rows[3].gradeName)
        assertEquals(0.0, rows[3].minPoints)
        assertEquals(17.5, rows[3].maxPoints)
    }

    @Test
    fun buildRows_reversesOrder_whenAscending() {
        val options = TableExportOptions(sortDescending = false)
        val rows = builder.buildRows(testGradeScale, options, totalPoints = 25.0)

        assertEquals(4, rows.size)
        assertEquals("F", rows[0].gradeName)
        assertEquals("C", rows[1].gradeName)
        assertEquals("B", rows[2].gradeName)
        assertEquals("A", rows[3].gradeName)
    }

    @Test
    fun buildRows_returnsEmptyList_whenGradesEmpty() {
        val emptyScale = GradeScale(id = "2", gradeScaleName = "Empty", totalPoints = 10.0, grades = persistentListOf())
        val rows = builder.buildRows(emptyScale, TableExportOptions(), 10.0)
        assertTrue(rows.isEmpty())
    }

    @Test
    fun htmlFormatter_includesExpectedStructureAndData() {
        val options = TableExportOptions(includePercentage = true, showAsRange = false)
        val rows = builder.buildRows(testGradeScale, options, totalPoints = 25.0)
        val html = htmlFormatter.format(rows, options)

        assertTrue(html.contains("<table"))
        assertTrue(html.contains("<thead>"))
        assertTrue(html.contains("<tbody>"))
        assertTrue(html.contains(">Grade</th>"))
        assertTrue(html.contains(">Points</th>"))
        assertTrue(html.contains(">Percentage</th>"))
        assertTrue(html.contains("≥ 22.5"))
        assertTrue(html.contains("≥ 90%"))
    }

    @Test
    fun htmlFormatter_supportsRangeFormat() {
        val options = TableExportOptions(includePercentage = true, showAsRange = true)
        val rows = builder.buildRows(testGradeScale, options, totalPoints = 25.0)
        val html = htmlFormatter.format(rows, options)

        assertTrue(html.contains("22.5 – 25"))
        assertTrue(html.contains("90% – 100%"))
    }

    @Test
    fun htmlFormatter_excludesPercentage_whenDisabled() {
        val options = TableExportOptions(includePercentage = false)
        val rows = builder.buildRows(testGradeScale, options, totalPoints = 25.0)
        val html = htmlFormatter.format(rows, options)

        assertFalse(html.contains(">Percentage</th>"))
        assertFalse(html.contains("≥ 90%"))
    }

    @Test
    fun htmlFormatter_returnsEmptyString_whenRowsEmpty() {
        assertEquals("", htmlFormatter.format(emptyList(), TableExportOptions()))
    }

    @Test
    fun tsvFormatter_producesTabSeparatedLines() {
        val options = TableExportOptions(includePercentage = true, showAsRange = false)
        val rows = builder.buildRows(testGradeScale, options, totalPoints = 25.0)
        val tsv = tsvFormatter.format(rows, options)

        val lines = tsv.lines()
        assertEquals(5, lines.size) // 1 header + 4 rows
        assertEquals("Grade\tPoints\tPercentage", lines[0])
        assertEquals("A\t≥ 22.5\t≥ 90%", lines[1])
        assertEquals("B\t≥ 20\t≥ 80%", lines[2])
    }

    @Test
    fun tsvFormatter_customHeaders() {
        val options = TableExportOptions(includePercentage = true)
        val rows = builder.buildRows(testGradeScale, options, totalPoints = 25.0)
        val headers = TableHeaders(gradeTitle = "Nota", pointsTitle = "Puntos", percentageTitle = "Porcentaje")
        val tsv = tsvFormatter.format(rows, options, headers)

        assertTrue(tsv.startsWith("Nota\tPuntos\tPorcentaje"))
    }

    @Test
    fun tsvFormatter_returnsEmptyString_whenRowsEmpty() {
        assertEquals("", tsvFormatter.format(emptyList(), TableExportOptions()))
    }

    @Test
    fun markdownFormatter_producesMarkdownTable() {
        val options = TableExportOptions(includePercentage = true, showAsRange = false)
        val rows = builder.buildRows(testGradeScale, options, totalPoints = 25.0)
        val md = mdFormatter.format(rows, options)

        val lines = md.lines()
        assertEquals(6, lines.size) // header + separator + 4 rows
        assertEquals("| Grade | Points | Percentage |", lines[0])
        assertTrue(lines[1].contains(":---:"))
        assertEquals("| A | ≥ 22.5 | ≥ 90% |", lines[2])
    }

    @Test
    fun markdownFormatter_returnsEmptyString_whenRowsEmpty() {
        assertEquals("", mdFormatter.format(emptyList(), TableExportOptions()))
    }

    @Test
    fun htmlFormatter_escapesHtmlSpecialCharacters() {
        val dangerousScale = GradeScale(
            id = "d1",
            gradeScaleName = "Test",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(namedGrade = "A & B <\"test\">", percentage = 0.90, idOfGradeScale = "d1", nameOfScale = "Test", uuid = "g1"),
            ),
        )
        val rows = builder.buildRows(dangerousScale, TableExportOptions(), 10.0)
        val headers = TableHeaders(gradeTitle = "Grade <1>", pointsTitle = "Points & Pts", percentageTitle = "Percentage > 0")
        val html = htmlFormatter.format(rows, TableExportOptions(includePercentage = true), headers)

        assertTrue(html.contains("A &amp; B &lt;&quot;test&quot;&gt;"))
        assertTrue(html.contains("Grade &lt;1&gt;"))
        assertTrue(html.contains("Points &amp; Pts"))
        assertTrue(html.contains("Percentage &gt; 0"))
        assertFalse(html.contains("<\"test\">"))
    }

    @Test
    fun markdownFormatter_escapesPipesAndLineBreaks() {
        val pipeScale = GradeScale(
            id = "p1",
            gradeScaleName = "Test",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(namedGrade = "Grade|1\nSub", percentage = 0.90, idOfGradeScale = "p1", nameOfScale = "Test", uuid = "g1"),
            ),
        )
        val rows = builder.buildRows(pipeScale, TableExportOptions(), 10.0)
        val headers = TableHeaders(gradeTitle = "Grade|Col")
        val md = mdFormatter.format(rows, TableExportOptions(), headers)

        assertTrue(md.contains("Grade\\|Col"))
        assertTrue(md.contains("Grade\\|1 Sub"))
    }

    @Test
    fun tsvFormatter_sanitizesTabsAndLineBreaks() {
        val tabScale = GradeScale(
            id = "t1",
            gradeScaleName = "Test",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(namedGrade = "Grade\t1\r\nSub", percentage = 0.90, idOfGradeScale = "t1", nameOfScale = "Test", uuid = "g1"),
            ),
        )
        val rows = builder.buildRows(tabScale, TableExportOptions(), 10.0)
        val headers = TableHeaders(gradeTitle = "Grade\tName")
        val tsv = tsvFormatter.format(rows, TableExportOptions(), headers)

        val lines = tsv.lines()
        assertEquals(2, lines.size) // header + 1 row
        assertTrue(lines[0].startsWith("Grade Name\t"))
        assertTrue(lines[1].startsWith("Grade 1  Sub\t"))
    }

    @Test
    fun buildRows_handlesBoundaryPercentages_100PercentAndZero() {
        val boundaryScale = GradeScale(
            id = "b1",
            gradeScaleName = "Boundary Scale",
            totalPoints = 100.0,
            grades = persistentListOf(
                Grade(namedGrade = "Top", percentage = 1.00, idOfGradeScale = "b1", nameOfScale = "Boundary Scale", uuid = "g1"),
                Grade(namedGrade = "Mid", percentage = 0.50, idOfGradeScale = "b1", nameOfScale = "Boundary Scale", uuid = "g2"),
                Grade(namedGrade = "Fail", percentage = 0.00, idOfGradeScale = "b1", nameOfScale = "Boundary Scale", uuid = "g3"),
            ),
        )
        val rows = builder.buildRows(boundaryScale, TableExportOptions(sortDescending = true), 100.0)

        assertEquals(3, rows.size)
        assertEquals("Top", rows[0].gradeName)
        assertEquals(100.0, rows[0].minPoints)
        assertEquals(100.0, rows[0].maxPoints)
        assertEquals(1.0, rows[0].minPercentage)
        assertEquals(1.0, rows[0].maxPercentage)

        assertEquals("Mid", rows[1].gradeName)
        assertEquals(50.0, rows[1].minPoints)
        assertEquals(100.0, rows[1].maxPoints)

        assertEquals("Fail", rows[2].gradeName)
        assertEquals(0.0, rows[2].minPoints)
        assertEquals(50.0, rows[2].maxPoints)
    }
}
