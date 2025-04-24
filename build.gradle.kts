import extensions.sonarQubeBaseConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

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
            (this as? KotlinJvmCompilerOptions)?.jvmTarget?.set(JvmTarget.fromTarget(libs2.versions.java.get()))
            optIn.addAll("kotlin.RequiresOptIn")
            freeCompilerArgs.addAll("-opt-in=kotlin.uuid.ExperimentalUuidApi", "-Xexpect-actual-classes")
        }
    }
}
