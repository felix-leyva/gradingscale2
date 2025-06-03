
import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import java.io.File

/**
 * Data class to hold Firebase configuration values
 */
data class FirebaseConfigData(
    val appId: String = "",
    val projectId: String = "",
    val apiKey: String = ""
)

/**
 * Extension function to configure Firebase BuildConfig fields
 * This approach is configuration cache compatible as it uses Gradle's Provider API
 */
fun Project.configureFirebaseBuildConfig(googleServicesPath: String = "composeApp/google-services.json") {
    // Create a provider that lazily reads the Firebase configuration
    val firebaseConfigProvider: Provider<FirebaseConfigData> = providers.provider {
        val googleServicesFile = rootProject.file(googleServicesPath)
        parseFirebaseConfig(googleServicesFile)
    }
    
    // Configure BuildConfig with the provider values
    extensions.configure<BuildConfigExtension> {
        className("BuildResources")
        packageName("de.felixlf.gradingscale2")
        
        // Map the provider to BuildConfig fields
        // The provider will only be evaluated when the buildConfig task runs
        firebaseConfigProvider.get().let { config ->
            buildConfigField("String", "FIREBASE_APP_ID", "\"${config.appId}\"")
            buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${config.projectId}\"")
            buildConfigField("String", "FIREBASE_API_KEY", "\"${config.apiKey}\"")
        }
    }
}

/**
 * Parse Firebase configuration from google-services.json file
 */
private fun parseFirebaseConfig(googleServicesFile: File): FirebaseConfigData {
    return if (googleServicesFile.exists()) {
        try {
            val json = Gson().fromJson(googleServicesFile.readText(), JsonObject::class.java)
            val client = json.getAsJsonArray("client").first().asJsonObject
            FirebaseConfigData(
                appId = client.get("client_info").asJsonObject.get("mobilesdk_app_id").asString,
                projectId = json.getAsJsonObject("project_info").get("project_id").asString,
                apiKey = client.getAsJsonArray("api_key").first().asJsonObject.get("current_key").asString
            )
        } catch (e: Exception) {
            println("Warning: Failed to parse Firebase configuration: ${e.message}")
            FirebaseConfigData()
        }
    } else {
        println("Warning: google-services.json not found at ${googleServicesFile.absolutePath}")
        FirebaseConfigData()
    }
}
