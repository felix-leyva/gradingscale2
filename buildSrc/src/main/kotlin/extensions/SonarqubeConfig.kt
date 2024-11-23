package extensions

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.sonarqube.gradle.SonarExtension

/**
 * Disable sonarqube analysis for this project
 */
fun Project.skipSonarqube() {
    extensions.configure<SonarExtension>("sonarqube") {
        isSkipProject = true
    }
}

fun Project.sonarQubeBaseConfig() {
    apply(plugin = "org.sonarqube")
    extensions.configure<SonarExtension>("sonarqube") {
        properties {
            property("sonar.sourceEncoding", "UTF-8")
            property("sonar.projectName", "GradingScale")
            property("sonar.projectKey", "de.felixlf.gradingscale")
            property("sonar.projectDescription", "GradingScale")
            property("sonar.branch.name", gitBranch())
            property("sonar.projectVersion", project.findProperty("version") ?: "")
            property("sonar.links.homepage", "")
            property("sonar.coverage.exclusions", exclusions.joinToString())
            property("sonar.language", "kotlin")
            property("sonar.java.coveragePlugin", "jacoco")
        }
    }

    subprojects {
        extensions.configure<SonarExtension>("sonarqube") {
            properties {
                property(
                    "sonar.coverage.jacoco.xmlReportPaths",
                    // Depends on the name of the jacoco task (which currently is jacocoReport)
                    "${project.buildDir}/reports/jacoco/jacocoReport/jacocoReport.xml",
                )
            }
        }
    }
}

private val exclusions =
    listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "android/**/*.*",
        "androidx/**/*.*",
        "**/event/**/*.*",
        "**/*EventType.*",
        // Generated Files
        // Test Classes
        "androidTest/**/*.*",
        "test/**/*.*",
        "**/mock/**/*.*",
        "**/**Mocked",
        "*/sharedtest/**/*.*",
        // Koin Modules
        "**/*Modules.*",
        "**/*Module.*",
        "**/*ModuleProvider.*",
        // tested by instrumented tests
        "**/*Activity.*",
        "**/*Event.*",
        "**/*Initializer.*",
        "**/*Service.*",
        "**/*Receiver.*",
        "**/*BackupAgent.*",
        "**/Notification*.*",
        "**/*ActivityTestRule.*",
        "**/src/main/res/**/*.*",
        "**/*Router.*",
        "**/*Extensions.*",
        "**/*Extension.*",
    )
