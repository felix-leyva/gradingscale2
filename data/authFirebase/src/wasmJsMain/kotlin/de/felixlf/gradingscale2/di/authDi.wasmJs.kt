@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.AuthInitializerImpl
import de.felixlf.gradingscale2.JSAnalyticsProviderImpl
import de.felixlf.gradingscale2.JSAuthTokenProviderImpl
import de.felixlf.gradingscale2.entities.network.AnalyticsProvider
import de.felixlf.gradingscale2.entities.network.AuthInitializer
import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal actual fun authPlatformModule(): Module =
    module {
        singleOf<AuthInitializer>(::AuthInitializerImpl)
        singleOf<AnalyticsProvider>(::JSAnalyticsProviderImpl)

        single<AuthTokenProvider> {
            JSAuthTokenProviderImpl(
                scope = CoroutineScope(
                    SupervisorJob() + Dispatchers.Default + CoroutineName("authScope"),
                ),
            )
        }
    }
