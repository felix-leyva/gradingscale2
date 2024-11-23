package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.AuthInitializer
import de.felixlf.gradingscale2.AuthInitializerImpl
import de.felixlf.gradingscale2.AuthTokenProvider
import de.felixlf.gradingscale2.AuthTokenProviderImpl
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal expect fun authPlatformModule(): Module

val authModule =
    module {
        includes(authPlatformModule())
        singleOf<AuthInitializer>(::AuthInitializerImpl)

        single<AuthTokenProvider> {
            AuthTokenProviderImpl(
                auth = Firebase.auth,
                scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("authScope")),
            )
        }
    }
