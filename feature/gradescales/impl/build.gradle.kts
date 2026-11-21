plugins {
    id("kmp-compose-feature-impl")
}

kotlin {
    androidLibrary {
        namespace = libs.versions.packagename + ".feature.gradescales.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.gradescales.api)
            implementation(projects.core.domain)
            implementation(projects.core.usecases.api)
            implementation(projects.core.presentation)
            implementation(projects.core.ui)
        }
    }
}
