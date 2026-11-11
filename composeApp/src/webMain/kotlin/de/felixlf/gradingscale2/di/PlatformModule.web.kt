package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.entities.features.export.TableClipboardExporter
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import de.felixlf.gradingscale2.export.WebTableClipboardExporter
import de.felixlf.gradingscale2.utils.DefaultDispatcherProvider
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

/**
 * Used to provide platform specific dependencies
 * @return The platform specific module
 */
actual fun getApplicationModule() =
    module {
        single<DefaultDispatcherProvider>() bind DispatcherProvider::class
        single<WebTableClipboardExporter>() bind TableClipboardExporter::class
    }
