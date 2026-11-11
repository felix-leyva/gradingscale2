@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.AuthInitializerImpl
import de.felixlf.gradingscale2.AuthTokenProviderImpl
import de.felixlf.gradingscale2.entities.network.AnalyticsProvider
import de.felixlf.gradingscale2.entities.network.AuthInitializer
import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single

private fun provideAuthTokenProvider(authInitializer: AuthInitializer): AuthTokenProvider {
    authInitializer()
    return AuthTokenProviderImpl(
        auth = Firebase.auth,
        scope = CoroutineScope(
            SupervisorJob() + Dispatchers.Default + CoroutineName("authScope"),
        ),
    )
}

internal actual fun authPlatformModule(): Module =
    module {
        single<JVMAnalyticsProviderImpl>() bind AnalyticsProvider::class
        single<AuthInitializerImpl>() bind AuthInitializer::class
        single { create(::provideAuthTokenProvider) }
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
