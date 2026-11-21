package de.felixlf.gradingscale2.core.ui.di

import androidx.compose.material3.SnackbarHostState
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase
import de.felixlf.gradingscale2.entities.util.GradeScaleGenerator
import de.felixlf.gradingscale2.entities.util.ResourceGradeScaleGenerator
import de.felixlf.gradingscale2.usecases.ShowSnackbarUseCaseImpl
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val coreUiModule = module {
    single<SnackbarHostState>()
    single<ShowSnackbarUseCaseImpl>() bind ShowSnackbarUseCase::class
    single<ResourceGradeScaleGenerator>() bind GradeScaleGenerator::class
}
