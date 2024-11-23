package de.felixlf.gradingscale2

import android.app.Application
import de.felixlf.gradingscale2.di.mainModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GradingScaleApp : Application() {
    val appInitializer: Initializer by inject()

    override fun onCreate() {
        super.onCreate()
        setupKoin()
        appInitializer()
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@GradingScaleApp)
            androidLogger()
            modules(mainModule)
        }
    }
}
