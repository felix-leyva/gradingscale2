import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.google.gson.Gson
import com.google.gson.JsonObject


plugins {
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
    id(libs2.plugins.gmazzoBuildConfig.get().pluginId)
}

kotlin {
    applyDefaultHierarchyTemplate()

    js {
        outputModuleName = "authfirebase"
        browser()
    }
    sourceSets {
        val firebaseAvailable by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs2.firebase.auth)
                implementation(libs2.firebase.analytics)
            }
        }

        androidMain {
            dependsOn(firebaseAvailable)
            dependencies {
                // Workaround for https://github.com/GitLiveApp/firebase-kotlin-sdk/issues/356
                implementation(project.dependencies.platform(libs2.firebase.bom))
                implementation(libs2.firebase.common.ktx)
                implementation(libs2.firebase.auth.ktx)
                //            implementation(libs2.firebase.crashlytics)
            }
        }

        commonMain {
        }

        jsMain.dependencies {
//            implementation(npm("@gitliveapp/firebase-auth", "1.5.19-beta"))
//            implementation(npm("@gitliveapp/firebase-common", "1.5.19-beta"))
        }
        iosMain {
            dependsOn(firebaseAvailable)
            dependencies {
                implementation(libs2.firebase.crashlytics)
            }
        }
        jvmMain.get().dependsOn(firebaseAvailable)
    }
}

android {
    namespace = libs.versions.packagename + ".authfirebase"
}

buildConfig {


}

// Generate Firebase constants for the JVM build which does not has a plugin to generate the configuration
val buildResources = buildConfig.forClass("BuildResources")
val generateResourcesConstants by tasks.registering {
    val googleJson = rootProject.file("composeApp/google-services.json")

    doFirst {
        var appId = ""
        var projectId = ""
        var apiKey = ""

        if (googleJson.exists()) {
            val json = Gson().fromJson(googleJson.readText(), JsonObject::class.java)
            appId = json.getAsJsonArray("client").first().asJsonObject.get("client_info").asJsonObject.get("mobilesdk_app_id").asString
            apiKey = json.getAsJsonArray("client").first().asJsonObject.getAsJsonArray("api_key")
                .first().asJsonObject.get("current_key").asString
            projectId = json.getAsJsonObject("project_info").get("project_id").asString
        }
        buildResources.apply {
            packageName("de.felixlf.gradingscale2")
            buildConfigField("String", "FIREBASE_APP_ID", "\"$appId\"")
            buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$projectId\"")
            buildConfigField("String", "FIREBASE_API_KEY", "\"$apiKey\"")
        }
    }
}

tasks.generateJvmMainNonAndroidBuildConfig.dependsOn(generateResourcesConstants)
