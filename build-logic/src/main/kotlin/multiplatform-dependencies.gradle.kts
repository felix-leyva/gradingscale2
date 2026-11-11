plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("io.insert-koin.compiler.plugin")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
        }

        androidMain.dependencies {
            implementation(libs.libraries.koinAndroid)
        }

        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.libraries.koinBom))
            implementation(libs.libraries.koinCore)
            implementation(libs.libraries.kotlinxCoroutinesCore)
            implementation(libs.libraries.arrowCore)
        }

        commonTest.dependencies {
            implementation(libs.libraries.junit)
            implementation(libs.libraries.kotlinTest)
            implementation(libs.libraries.koinTest)
            implementation(libs.libraries.turbine)
            implementation(libs.libraries.coroutinesTest)
        }

        iosMain.dependencies {
        }
    }
}
