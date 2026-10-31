@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import java.util.Properties

plugins {
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
    id(libs2.plugins.ksp.get().pluginId)
    id(libs2.plugins.gmazzoBuildConfig.get().pluginId)
}

kotlin {

    wasmJs {
        browser {
            testTask {
                enabled = false
            }
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(libs2.ktor.client.okhttp)
        }

        commonMain.dependencies {
            implementation(libs2.ktor.client.core)
            implementation(libs2.ktor.client.content.negotiation)
            implementation(libs2.ktor.serialization.kotlinx.json)
            implementation(libs2.ktor.client.logging)
            implementation(libs2.ktor.client.auth)
            implementation(projects.entities)
        }

        commonTest.dependencies {
            implementation(libs2.ktor.client.test)
        }

        jvmMain.dependencies {
            implementation(libs2.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs2.ktor.client.darwin)
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs2.ktor.client.js)
            }
        }
    }
}

kotlin {
    androidLibrary {
        namespace = libs.versions.packagename + ".network"
    }
}

// Generate Firebase constants for the JVM build which does not has a plugin to generate the configuration
buildConfig {
    forClass("BuildBaseUrls") {
        packageName("de.felixlf.gradingscale2")

        // Read the property at configuration time, not task execution time
        // (plain Properties instead of AGP's internal gradleLocalProperties, removed in AGP 9)
        val localProperties = Properties().apply {
            val localPropertiesFile = rootDir.resolve("local.properties")
            if (localPropertiesFile.exists()) {
                localPropertiesFile.inputStream().use { load(it) }
            }
        }
        val baseUrl = localProperties.getProperty("GRADINGSCALE_BASE_URL") ?: ""

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }
}

val pitestConfiguration: Configuration = configurations.create("pitest")

dependencies {
    add("pitest", libs2.pitest.command.line)
}

val pitestJvm = tasks.register<JavaExec>("pitestJvm") {
    group = "verification"
    description = "Runs PITest mutation testing on JVM target."
    dependsOn("jvmTestClasses", "compileKotlinJvm")
    notCompatibleWithConfigurationCache("PITest dynamically reads test classpath and writes report")

    val testClasspath = files(tasks.named<Test>("jvmTest").map { it.classpath })
    val sourceDirs = listOf(
        file("src/commonMain/kotlin"),
        file("src/jvmMain/kotlin"),
    )

    classpath = pitestConfiguration
    mainClass.set("org.pitest.mutationtest.commandline.MutationCoverageReport")

    val targetClassesProp = providers.gradleProperty("pitest.targetClasses")
        .orElse("de.felixlf.gradingscale2.network.api.*")
    val targetTestsProp = providers.gradleProperty("pitest.targetTests")
        .orElse("de.felixlf.gradingscale2.network.api.*Test")
    val mutatorsProp = providers.gradleProperty("pitest.mutators")
        .orElse("STRONGER")
    val reportDirFile = layout.buildDirectory.dir("reports/pitest")
    val classpathFile = layout.buildDirectory.file("pitest/classpath.txt")

    doFirst {
        val cpFile = classpathFile.get().asFile
        cpFile.parentFile.mkdirs()
        cpFile.writeText(
            testClasspath.filter { it.exists() }.joinToString("\n") { it.absolutePath }
        )

        val reportDir = reportDirFile.get().asFile
        reportDir.mkdirs()

        args(
            "--reportDir", reportDir.absolutePath,
            "--targetClasses", targetClassesProp.get(),
            "--targetTests", targetTestsProp.get(),
            "--excludedClasses", "*Test*",
            "--sourceDirs", sourceDirs.filter { it.exists() }.joinToString(",") { it.absolutePath },
            "--classPathFile", cpFile.absolutePath,
            "--avoidCallsTo", "kotlin.jvm.internal,org.slf4j,java.util.logging",
            "--outputFormats", "HTML,XML,CSV",
            "--timestampedReports=false",
            "--mutators", mutatorsProp.get(),
            "--threads", "4"
        )
    }
}

