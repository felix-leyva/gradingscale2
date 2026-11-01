import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("sonarqube-base")
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
    alias(libs2.plugins.kover)
}

dependencies {
    kover(project(":entities"))
    kover(project(":data:network"))
    kover(project(":data:persistance:db"))
    kover(project(":composeApp"))
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*ComposableSingletons*",
                    "*$\$serializer",
                    "*.BuildConfig",
                )
                packages(
                    "gradingscale2.composeapp.generated.resources",
                    "gradingscale2.entities.generated.resources",
                )
            }
        }
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlinx.kover")

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
