package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Either
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepositoryImpl
import de.felixlf.gradingscale2.entities.repositories.GradesRepository
import de.felixlf.gradingscale2.entities.repositories.GradesRepositoryImpl
import de.felixlf.gradingscale2.entities.repositories.MockGradeScaleDao
import de.felixlf.gradingscale2.entities.repositories.MockGradesDao
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class InsertGradeUseCaseImplTest {
    private val dao: MockGradeScaleDao = MockGradeScaleDao()
    private val gradesDao: MockGradesDao = MockGradesDao()
    private lateinit var gradesRepository: GradesRepository
    private lateinit var gradeScaleRepo: GradeScaleRepositoryImpl
    private lateinit var useCase: InsertGradeUseCase

    @BeforeTest
    fun setup() {
        dao.success = true
        dao.gradeScales.value = MockGradeScalesGenerator().gradeScales
        gradesDao.success = true
        gradesDao.gradeScales.value = MockGradeScalesGenerator().gradeScales
        gradeScaleRepo = GradeScaleRepositoryImpl(gradeScaleDao = dao)
        gradesRepository = GradesRepositoryImpl(gradesDao = gradesDao)
        useCase = InsertGradeUseCaseImpl(gradeScaleRepository = gradeScaleRepo, gradesRepository = gradesRepository)
    }

    @Test
    fun `invoke should insert grade when grade scale exists and no grade with same name or percentage`() = runTest {
        // Given
        val gradeScale = dao.gradeScales.value.first()
        val gradeScaleId = gradeScale.id
        val percentage = 0.86
        val namedGrade = "A1"

        // When
        val result = useCase(gradeScaleId, percentage, namedGrade)

        // Assert
        assertEquals(result, Either.Right(Unit))
    }

    @Test
    fun `invoke should fail to insert grade when a grade with same name already exists`() = runTest {
        // Given
        val gradeScale = dao.gradeScales.value.first()
        val gradeScaleId = gradeScale.id
        val percentage = 0.86
        val namedGrade = gradeScale.grades.first().namedGrade

        // When
        val result = useCase(gradeScaleId, percentage, namedGrade)

        // Assert
        assertEquals(result, Either.Left(InsertGradeUseCaseError.GradeWithSameNameAlreadyExists))
    }

    @Test
    fun `invoke should fail to insert grade when a grade with same percentage already exists`() = runTest {
        // Given
        val gradeScale = dao.gradeScales.value.first()
        val gradeScaleId = gradeScale.id
        val percentage = gradeScale.grades.first().percentage
        val namedGrade = "A1"

        // When
        val result = useCase(gradeScaleId, percentage, namedGrade)

        // Assert
        assertEquals(result, Either.Left(InsertGradeUseCaseError.PercentageAlreadyExists))
    }

    @Test
    fun `invoke should fail to insert grade when grade scale does not exist`() = runTest {
        // Given
        val gradeScaleId = "non-existing"
        val percentage = 0.86
        val namedGrade = "A1"

        // When
        val result = useCase(gradeScaleId, percentage, namedGrade)

        // Assert
        assertEquals(result, Either.Left(InsertGradeUseCaseError.GradeScaleIdNotFound))
    }

    @Test
    fun `invoke should fail to insert grade when error inserting grade`() = runTest {
        // Given
        val gradeScale = dao.gradeScales.value.first()
        val gradeScaleId = gradeScale.id
        val percentage = 0.86
        val namedGrade = "A1"
        gradesDao.success = false

        // When
        val result = useCase(gradeScaleId, percentage, namedGrade)

        // Assert
        assertEquals(result, Either.Left(InsertGradeUseCaseError.ErrorInsertingGrade))
    }
}
