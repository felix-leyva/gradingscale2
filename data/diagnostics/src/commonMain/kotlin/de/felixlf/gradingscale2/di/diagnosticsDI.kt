@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.CommonDiagnosticsProviderImpl
import de.felixlf.gradingscale2.entities.network.DiagnosticsProvider
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

internal val defaultDiagnosticsModule = module {
    single<CommonDiagnosticsProviderImpl>() bind DiagnosticsProvider::class
}

internal expect fun nativeDiagnosticsModule(): Module

val diagnosticsModule = module {
    includes(nativeDiagnosticsModule())
}
