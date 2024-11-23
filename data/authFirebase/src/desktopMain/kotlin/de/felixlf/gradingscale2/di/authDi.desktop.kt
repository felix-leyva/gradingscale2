@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.AnalyticsProvider
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun authPlatformModule(): Module =
    module {
        single<AnalyticsProvider> { JVMAnalyticsProviderImpl() }
    }

// In JVM enviroment, the analytics provider is not yet implemented
internal class JVMAnalyticsProviderImpl : AnalyticsProvider {
    override fun logEvent(
        name: String,
        params: Map<String, Any>?,
    ) {
        println("Logging event $name with params $params")
    }
}
