import extensions.sonarQubeBaseConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

// jacocoBaseConfig()
sonarQubeBaseConfig()
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
}
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        version.set("0.50.0")
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(true)
        enableExperimentalRules.set(true)
    }
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll("-XXLanguage:+ExplicitBackingFields", "-opt-in=kotlin.uuid.ExperimentalUuidApi")
        }
    }
}
