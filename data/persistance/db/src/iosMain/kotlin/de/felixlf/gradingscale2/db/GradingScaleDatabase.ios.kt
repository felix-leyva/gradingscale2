@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import de.felixlf.gradingscale2.Database

internal class IOSDriverFactory : DriverFactory {
    override fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = Database.Schema.synchronous(),
        name = "gradingscale.db",
        onConfiguration = { config ->
            config.copy(
                extendedConfig = config.extendedConfig.copy(foreignKeyConstraints = true),
            )
        },
    )
}
