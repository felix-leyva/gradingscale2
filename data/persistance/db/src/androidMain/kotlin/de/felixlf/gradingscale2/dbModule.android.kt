@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.db.AndroidDriverFactory
import de.felixlf.gradingscale2.db.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

internal actual fun getDbPlatformModule(): Module =
    module {
        single<AndroidDriverFactory>() bind DriverFactory::class
        includes(sqlDaoModule)
    }
