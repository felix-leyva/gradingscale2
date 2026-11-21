plugins {
    id("kmp-library-pure")
    id("io.insert-koin.compiler.plugin")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.libraries.koinAndroid)
        }

        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.libraries.koinBom))
            implementation(libs.libraries.koinCore)
        }

        commonTest.dependencies {
            implementation(libs.libraries.koinTest)
        }
    }
}
