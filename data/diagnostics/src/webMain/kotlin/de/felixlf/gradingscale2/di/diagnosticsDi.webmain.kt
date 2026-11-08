@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.WebMainDiagnosticsProviderImpl
import de.felixlf.gradingscale2.entities.network.DiagnosticsProvider
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

internal actual fun nativeDiagnosticsModule(): Module =
    module {
        single<WebMainDiagnosticsProviderImpl>() bind DiagnosticsProvider::class
    }
