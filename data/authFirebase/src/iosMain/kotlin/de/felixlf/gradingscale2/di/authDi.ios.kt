package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.AnalyticsProvider
import de.felixlf.gradingscale2.AnalyticsProviderBaseImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal actual fun authPlatformModule(): Module =
    module {
        singleOf<AnalyticsProvider>(::AnalyticsProviderBaseImpl)
    }
