plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation(libs.gradle.agp)
    implementation(libs.gradle.jetbrainsCompose)
    implementation(libs.gradle.compose.compiler)
    implementation(libs.gradle.kotlinMultiplatform)
    implementation(libs.gradle.ksp)
    implementation(libs.gradle.google.services)
    implementation(libs.gradle.kotlinxSerialization)
    implementation(libs.gradle.gmazzoBuildConfig)
    implementation(libs.gradle.aboutLibraries)
    implementation(libs.gradle.sonarqube)
    implementation(libs.gradle.firebase.crashlytics.buildtools)
    implementation(libs.gradle.sqldelight)
}
