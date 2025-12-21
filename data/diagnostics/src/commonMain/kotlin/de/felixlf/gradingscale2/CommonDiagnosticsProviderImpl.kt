package de.felixlf.gradingscale2

import GradingScale2.data.diagnostics.BuildConfig
import de.felixlf.gradingscale2.entities.network.DiagnosticsProvider
import io.sentry.kotlin.multiplatform.Sentry
internal class CommonDiagnosticsProviderImpl : DiagnosticsProvider {
    override suspend fun initDiagnostics() {
        Sentry.init { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.sendDefaultPii = false
            options.attachViewHierarchy = true
        }
    }
}
