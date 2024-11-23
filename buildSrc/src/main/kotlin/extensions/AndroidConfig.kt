package extensions

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import libs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get

fun Project.baseAndroidConfig(baseExtension: BaseExtension) {
    baseExtension.apply {
        compileSdkVersion(libs.versions.androidCompileSdk.toInt())
        sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
        sourceSets["main"].res.srcDirs("src/androidMain/res")
        sourceSets["main"].resources.srcDirs("src/commonMain/resources")

        defaultConfig {
            minSdk = libs.versions.androidMinSdk.toInt()
            if (this@apply is BaseAppModuleExtension) {
                targetSdk = libs.versions.androidTargetSdk.toInt()
                applicationId = libs.versions.applicationId
                versionCode = libs.versions.versionCode.toInt()
                versionName = libs.versions.versionName
            }
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        if (this@apply is LibraryExtension) {
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
        }

        buildTypes {
            getByName("release") {
                if (this@apply is LibraryExtension) consumerProguardFile("consumer-rules.pro")
                proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(libs.versions.java)
            targetCompatibility = JavaVersion.toVersion(libs.versions.java)
        }
    }
    dependencies {
        add("debugImplementation", libs.libraries.composePreviewTooling)
    }
}

fun Project.setupCompose(baseExtension: BaseExtension) {
    baseExtension.apply {
        composeOptions {
            buildFeatures.compose = true
        }
    }
}
