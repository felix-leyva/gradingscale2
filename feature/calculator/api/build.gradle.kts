plugins {
    id("kmp-compose-feature-api")
}

kotlin {
    androidLibrary {
        namespace = libs.versions.packagename + ".feature.calculator.api"
    }
}
