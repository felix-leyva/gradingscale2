@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return WebWorkerDriver(
            Worker(
                js(
                    """new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""",
                ),
            ),
        )
        // Create the schema on start for the js version
        // .also { Database.Schema.create(it).await() }
    }
}
