package de.felixlf.gradingscale2

import android.app.Application
import de.felixlf.gradingscale2.di.koinSetup
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class GradingScaleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupKoin()
    }

    private fun setupKoin() {
        koinSetup {
            androidContext(this@GradingScaleApp)
            androidLogger()
        }
    }
}
