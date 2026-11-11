@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.di

import org.koin.core.module.Module

internal actual fun nativeDiagnosticsModule(): Module = defaultDiagnosticsModule
