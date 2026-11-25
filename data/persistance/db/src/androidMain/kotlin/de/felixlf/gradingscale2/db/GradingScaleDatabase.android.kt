@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import de.felixlf.gradingscale2.Database

internal class AndroidDriverFactory(
    private val context: Context,
) : DriverFactory {
    override fun createDriver(): SqlDriver {
        val schema = Database.Schema.synchronous()
        return AndroidSqliteDriver(
            schema = schema,
            context = context,
            name = "gradingscale.db",
            callback = object : AndroidSqliteDriver.Callback(schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.setForeignKeyConstraintsEnabled(true)
                }
            },
        )
    }
}
