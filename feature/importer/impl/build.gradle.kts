plugins {
    id("kmp-compose-feature-impl")
}

kotlin {
    androidLibrary {
        namespace = libs.versions.packagename + ".feature.importer.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.importer.api)
            implementation(projects.core.domain)
            implementation(projects.core.usecases.api)
            implementation(projects.core.presentation)
            implementation(projects.core.ui)
        }
    }
}
