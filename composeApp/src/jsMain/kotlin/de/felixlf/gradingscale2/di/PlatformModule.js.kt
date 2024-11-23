package de.felixlf.gradingscale2.di

import org.koin.dsl.module

/**
 * Used to provide platform specific dependencies
 * @return The platform specific module
 */
actual fun getApplicationModule() =
    module {
        // No Op
    }
