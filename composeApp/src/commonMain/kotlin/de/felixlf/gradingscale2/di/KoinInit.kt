package de.felixlf.gradingscale2.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun koinSetup(config: KoinAppDeclaration? = null) =
    startKoin {
        config?.invoke(this)
        modules(mainModule)
        printLogger()
    }
