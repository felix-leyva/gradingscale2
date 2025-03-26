plugins {
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
    id(libs2.plugins.compose.compiler.get().pluginId)
    id(libs2.plugins.jetbrainsCompose.get().pluginId)
}

kotlin {
    js {
        browser {
            testTask {
                onlyIf { !System.getenv().containsKey("CI") }
                useKarma {
                    useFirefox()
                }
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.components.resources)
            implementation(libs2.molecule.runtime)
            implementation(libs2.kotlinx.collections.immutable)
            implementation(libs2.kotlinx.serialization)
        }
    }
    compilerOptions {
        freeCompilerArgs.addAll("-XXLanguage:+ExplicitBackingFields", "-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
}

android {
    namespace = libs.versions.packagename + ".entities"
    // This is important to be able to run unit test when using molecule, due the MonotonicClock dependency
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}
dependencies {
    testImplementation(project(":entities"))
}
