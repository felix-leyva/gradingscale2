plugins {
    id("kmp-library-pure")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
    id(libs2.plugins.gmazzoBuildConfig.get().pluginId)
}

kotlin {
    androidLibrary {
        namespace = libs.versions.packagename + ".core.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs2.kotlinx.serialization)
        }
    }
}

buildConfig {
    className("BuildConfigs")
    val packageName = libs2.versions.applicationId.get()
    useKotlinOutput { internalVisibility = false }
    packageName(packageName)
    buildConfigField("String", "PACKAGE_NAME", "\"$packageName\"")
    buildConfigField("String", "VERSION_CODE", "\"${libs2.versions.versionCode.get()}\"")
    buildConfigField("String", "ORGANIZATION", "\"${libs2.versions.organization.get()}\"")
}
