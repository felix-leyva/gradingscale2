// Android entry point for the app. With AGP 9 the Kotlin Multiplatform plugin no longer works with
// com.android.application, so this thin module hosts the Activity/Application/manifest while all
// shared code (including the androidMain actuals) lives in :composeApp via the Android-KMP library plugin.
plugins {
    id(libs2.plugins.androidApplication.get().pluginId)
    id(libs2.plugins.compose.compiler.get().pluginId)
    id(libs2.plugins.google.services.get().pluginId)
    id("sentry-android")
}

android {
    namespace = libs2.versions.applicationId.get()
    compileSdk = libs2.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = libs2.versions.applicationId.get()
        minSdk = libs2.versions.android.minSdk.get().toInt()
        targetSdk = libs2.versions.android.targetSdk.get().toInt()
        versionCode = libs2.versions.versionCode.get().toInt()
        versionName = libs2.versions.versionName.get()
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs2.versions.java.get())
        targetCompatibility = JavaVersion.toVersion(libs2.versions.java.get())
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs2.androidx.activity.compose)
    implementation(libs2.koin.android)
    debugImplementation(libs2.compose.preview.tooling)
}

tasks.register("checkAndCreateGoogleServices") {
    val googleServicesFile = layout.projectDirectory.file("google-services.json")
    val googleServicesContent = providers.environmentVariable("GOOGLE_SERVICES")

    doLast {
        if (!googleServicesFile.asFile.exists()) {
            val content = googleServicesContent.orNull
            if (content != null) {
                googleServicesFile.asFile.writeText(content)
                println("google-services.json file created")
            } else {
                println("Environment variable GOOGLE_SERVICES is not set.")
            }
        } else {
            println("google-services.json file already exists.")
        }
    }
}

tasks.register("checkAndCreateIosGoogleServices") {
    val googleServicesFile = rootProject.layout.projectDirectory.file("iosApp/iosApp/GoogleService-Info.plist")
    val googleServicesContent = providers.environmentVariable("GOOGLE_IOS_SECRET")
    doLast {
        if (!googleServicesFile.asFile.exists()) {
            val content = googleServicesContent.orNull
            if (content != null) {
                googleServicesFile.asFile.writeText(content)
                println("GoogleService-Info.plist file created")
            } else {
                println("Environment variable GOOGLE_IOS_SECRET is not set.")
            }
        } else {
            println("GoogleService-Info.plist file already exists.")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("checkAndCreateGoogleServices", "checkAndCreateIosGoogleServices")
}
