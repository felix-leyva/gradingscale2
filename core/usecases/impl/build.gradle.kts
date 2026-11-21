plugins {
    id("kmp-library-di")
}

kotlin {
    androidLibrary {
        namespace = libs.versions.packagename + ".core.usecases.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.usecases.api)
            implementation(libs2.napier)
        }
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
        .orElse("de.felixlf.gradingscale2.entities.*")
    val targetTestsProp = providers.gradleProperty("pitest.targetTests")
        .orElse("de.felixlf.gradingscale2.entities.*Test")
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
