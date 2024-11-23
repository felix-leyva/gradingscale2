package extensions

import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named

fun Project.ktlintConfig() {
    val ktlintConfig by configurations.creating

    dependencies {
        ktlintConfig("com.pinterest:ktlint:_") {
            attributes {
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
            }
        }
    }

    val outputDir = "${project.buildDir}/reports/ktlint/"
    val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))

    tasks.create<JavaExec>("ktlintCheck") {
        inputs.files(inputFiles)
        outputs.dir(outputDir)
        group = "verification"
        description = "Check Kotlin code style."
        classpath = ktlintConfig
        mainClass.set("com.pinterest.ktlint.Main")
        args = listOf("src/**/*.kt")
    }

    tasks.create<JavaExec>("ktlintFormat") {
        inputs.files(inputFiles)
        outputs.dir(outputDir)
        group = "formatting"
        description = "Fix Kotlin code style deviations."
        classpath = ktlintConfig
        mainClass.set("com.pinterest.ktlint.Main")
        args = listOf("-F", "src/**/*.kt")
    }
}
