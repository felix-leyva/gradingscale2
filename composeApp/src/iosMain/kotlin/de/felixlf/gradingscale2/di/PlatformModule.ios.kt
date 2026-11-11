package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.entities.features.export.TableClipboardExporter
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import de.felixlf.gradingscale2.export.IosTableClipboardExporter
import de.felixlf.gradingscale2.utils.DefaultDispatcherProvider
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

actual fun getApplicationModule() =
    module {
        single<DefaultDispatcherProvider>() bind DispatcherProvider::class
        single<IosTableClipboardExporter>() bind TableClipboardExporter::class
    }
