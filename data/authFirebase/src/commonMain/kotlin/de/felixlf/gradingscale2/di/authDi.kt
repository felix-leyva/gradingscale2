@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.di

import org.koin.core.module.Module
import org.koin.dsl.module

internal expect fun authPlatformModule(): Module

val authModule =
    module {
        includes(authPlatformModule())
        
    }
