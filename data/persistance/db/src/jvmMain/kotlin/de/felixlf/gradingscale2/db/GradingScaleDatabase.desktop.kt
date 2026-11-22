@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import de.felixlf.gradingscale2.BuildConfigs.ORGANIZATION
import de.felixlf.gradingscale2.BuildConfigs.PACKAGE_NAME
import net.harawata.appdirs.AppDirsFactory
import java.io.File
import java.nio.file.Files

internal class JVMDriverFactory : DriverFactory {
    override fun createDriver(): SqlDriver {
        val dbDir = System.getProperty("gradingscale.db.dir")
            ?.takeIf { it.isNotBlank() }
            ?: AppDirsFactory.getInstance().getUserDataDir(PACKAGE_NAME, null, ORGANIZATION)
        val dbFile = File(dbDir, "gradingscale.db")
        dbFile.parentFile?.let { Files.createDirectories(it.toPath()) }
        return JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
    }
}
