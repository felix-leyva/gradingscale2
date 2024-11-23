@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
//            Init the schema on start
//            .also { Database.Schema.create(it).await() }
    }
}
