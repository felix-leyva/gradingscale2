package de.felixlf.gradingscale2.core.presentation.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformPresentationModule: Module

val presentationModule = module {
    includes(platformPresentationModule)
}
