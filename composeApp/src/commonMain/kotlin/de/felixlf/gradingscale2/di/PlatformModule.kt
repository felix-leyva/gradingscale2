package de.felixlf.gradingscale2.di

import org.koin.core.module.Module

/**
 * Used to provide platform specific dependencies
 * @return The platform specific module
 */
expect fun getApplicationModule(): Module

