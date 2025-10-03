@file:OptIn(ExperimentalWasmJsInterop::class)

package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AnalyticsProvider

class JSAnalyticsProviderImpl : AnalyticsProvider {
    override fun logEvent(
        name: String,
        params: Map<String, Any>?,
    ) {
        try {
            val analytics = getFirebaseAnalytics()
            if (analytics != null && isFirebaseInitialized()) {
                // Convert parameters to JS object if provided
                val jsParams = if (params != null && params.isNotEmpty()) {
                    val obj = createJsObject()
                    params.forEach { (key, value) ->
                        when (value) {
                            is String -> setJsProperty(obj, key, value)
                            is Int -> setJsNumberProperty(obj, key, value)
                            is Long -> setJsNumberProperty(obj, key, value.toInt())
                            is Float -> setJsNumberProperty(obj, key, value.toInt())
                            is Double -> setJsNumberProperty(obj, key, value.toInt())
                            is Boolean -> setJsBooleanProperty(obj, key, value)
                            else -> setJsProperty(obj, key, value.toString())
                        }
                    }
                    obj
                } else {
                    null
                }

                // Log event to Firebase Analytics
                analytics.logEvent(name, jsParams)

                // Also log to console for debugging
                val paramsStr = params?.entries?.joinToString(", ") { "${it.key}=${it.value}" } ?: ""
                logToConsole("Firebase Analytics Event: $name${if (paramsStr.isNotEmpty()) " [$paramsStr]" else ""}")
            } else {
                // Fallback: Log to console when Firebase is not available
                val paramsStr = params?.entries?.joinToString(", ") { "${it.key}=${it.value}" } ?: ""
                logToConsole("Analytics Event (fallback): $name${if (paramsStr.isNotEmpty()) " [$paramsStr]" else ""}")
            }
        } catch (e: Throwable) {
            errorToConsole("Error logging analytics event: $name", e.toString())
            // Still log to console as fallback
            val paramsStr = params?.entries?.joinToString(", ") { "${it.key}=${it.value}" } ?: ""
            logToConsole("Analytics Event (error fallback): $name${if (paramsStr.isNotEmpty()) " [$paramsStr]" else ""}")
        }
    }
}
