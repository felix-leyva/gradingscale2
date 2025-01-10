@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.db

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import de.felixlf.gradingscale2.Database

internal class AndroidDriverFactory(
    private val context: Context,
) : DriverFactory {
    override fun createDriver(): SqlDriver =
        AndroidSqliteDriver(
            schema = Database.Schema.synchronous(),
            context = context,
            name = "gradingscale.db",
        )
}
