@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.db.AndroidDriverFactory
import de.felixlf.gradingscale2.db.DriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun getDbPlatformModule(): Module =
    module {
        single<DriverFactory> { AndroidDriverFactory(androidContext()) }
        includes(sqlDaoModule)
    }
