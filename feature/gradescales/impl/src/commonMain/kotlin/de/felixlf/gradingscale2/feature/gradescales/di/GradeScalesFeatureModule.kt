package de.felixlf.gradingscale2.feature.gradescales.di

import de.felixlf.gradingscale2.entities.features.export.ExportExamTableUIModel
import de.felixlf.gradingscale2.entities.features.list.GradeListUIModel
import de.felixlf.gradingscale2.entities.features.list.upsertgradedialog.UpsertGradeUIFactory
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIStateFactory
import de.felixlf.gradingscale2.feature.gradescales.api.GradeScalesScreenEntry
import de.felixlf.gradingscale2.features.list.GradeScaleListScreen
import de.felixlf.gradingscale2.features.list.GradeScaleListViewModel
import de.felixlf.gradingscale2.features.list.exportdialog.ExportExamTableViewModel
import de.felixlf.gradingscale2.features.list.upsertgradedialog.UpsertGradeViewModel
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleViewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.viewModel

val gradeScalesFeatureModule = module {
    factory<GradeListUIModel>()
    factory<UpsertGradeUIFactory>()
    factory<UpsertGradeScaleUIStateFactory>()
    factory<ExportExamTableUIModel>()
    viewModel<GradeScaleListViewModel>()
    viewModel<UpsertGradeViewModel>()
    viewModel<UpsertGradeScaleViewModel>()
    viewModel<ExportExamTableViewModel>()
    single<GradeScalesScreenEntry> {
        GradeScalesScreenEntry { modifier ->
            GradeScaleListScreen(modifier = modifier)
        }
    }
}
