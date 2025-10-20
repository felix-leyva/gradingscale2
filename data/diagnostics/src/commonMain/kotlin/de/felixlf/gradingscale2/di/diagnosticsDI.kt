@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.CommonDiagnosticsProviderImpl
import de.felixlf.gradingscale2.entities.network.DiagnosticsProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val defaultDiagnosticsModule = module {
    singleOf<DiagnosticsProvider>(::CommonDiagnosticsProviderImpl)
}

internal expect val nativeDiagnosticsModule: Module

// Lazy delegate, to avoid risk of circular dependency issue
val diagnosticsModule by lazy {
    module {
        includes(nativeDiagnosticsModule)
    }
}
