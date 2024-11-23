import extensions.baseAndroidConfig
import extensions.setupCompose

plugins {
    id("com.android.application")
}
android {
    baseAndroidConfig(this)
    setupCompose(this)
}
