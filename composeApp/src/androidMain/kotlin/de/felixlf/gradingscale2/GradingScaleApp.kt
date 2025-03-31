package de.felixlf.gradingscale2

import android.app.Application
import de.felixlf.gradingscale2.di.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GradingScaleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupKoin()
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@GradingScaleApp)
            androidLogger()
            modules(mainModule)
        }
    }
}
