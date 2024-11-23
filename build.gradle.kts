import extensions.sonarQubeBaseConfig

// jacocoBaseConfig()
sonarQubeBaseConfig()
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        version.set("0.50.0")
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
        filter {
            exclude("**/build/**")
        }
    }
}
