@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.AuthInitializerImpl
import de.felixlf.gradingscale2.entities.network.AuthInitializer
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal actual fun authPlatformModule(): Module =
    module {
        singleOf<AuthInitializer>(::AuthInitializerImpl)
    }
