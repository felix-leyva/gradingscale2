@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.WebMainDiagnosticsProviderImpl
import de.felixlf.gradingscale2.entities.network.DiagnosticsProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal actual val nativeDiagnosticsModule: Module =
    module {
        singleOf<DiagnosticsProvider>(::WebMainDiagnosticsProviderImpl)
    }
