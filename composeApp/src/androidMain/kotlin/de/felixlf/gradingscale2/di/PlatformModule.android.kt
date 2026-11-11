package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.entities.features.export.TableClipboardExporter
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import de.felixlf.gradingscale2.export.AndroidTableClipboardExporter
import de.felixlf.gradingscale2.util.AndroidDispatcherProvider
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

actual fun getApplicationModule() =
    module {
        single<AndroidDispatcherProvider>() bind DispatcherProvider::class
        single<AndroidTableClipboardExporter>() bind TableClipboardExporter::class
    }
