@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.db

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}
