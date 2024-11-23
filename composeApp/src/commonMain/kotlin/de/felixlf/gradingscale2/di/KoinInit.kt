package de.felixlf.gradingscale2.di

fun koinSetup() =
    org.koin.core.context.startKoin {
        modules(mainModule)
        printLogger()
    }
