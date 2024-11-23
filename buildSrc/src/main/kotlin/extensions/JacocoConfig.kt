package extensions

import com.android.build.api.variant.Variant
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.sonarqube.gradle.SonarExtension
import java.util.Locale

fun Project.jacocoBaseConfig() {
    subprojects {
        apply(plugin = "org.gradle.jacoco")
        extensions.configure<JacocoPluginExtension>("jacoco") {
            toolVersion = "0.8.8"
        }
    }
}

fun Project.configureJacocoAndSonarqube(buildVariant: Variant) {
    val name = buildVariant.name
    val nameCapitalized = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    val sources =
        buildVariant.sources.kotlin
            ?.all
            ?.get()
            ?.map { it.asFile }
            ?.filter { it.exists() } ?: error("Missing kotlin sources")
    val testTaskName = "test${nameCapitalized}UnitTest"
    tasks.create<JacocoReport>(name = "jacocoReport") {
        dependsOn(testTaskName)
        reports {
            xml.required.set(true)
        }

        classDirectories.setFrom(
            fileTree("${project.buildDir}/tmp/kotlin-classes/$name"),
        )

        sourceDirectories.setFrom(sources)

        executionData.setFrom(
            fileTree(project.buildDir) {
                include(
                    "outputs/unit_test_code_coverage/${name}UnitTest/$testTaskName.exec",
                )
            },
        )
    }
    extensions.configure<SonarExtension>("sonarqube") {
        properties {
            property("sonar.junit.reportsPath", "${project.buildDir}/reports/tests/$testTaskName")
            property("sonar.sources", sources)
        }
    }
}
