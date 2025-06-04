package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AnalyticsProvider
import kotlin.js.json

class JSAnalyticsProviderImpl : AnalyticsProvider {
    private val analytics by lazy {
        if (isFirebaseInitialized()) {
            getFirebaseAnalytics()
        } else {
            console.warn("Firebase not initialized, analytics will not be sent")
            null
        }
    }

    override fun logEvent(
        name: String,
        params: Map<String, Any>?,
    ) {
        try {
            analytics?.let { analyticsInstance ->
                val jsParams = params?.let { map ->
                    json().apply {
                        map.forEach { (key, value) ->
                            this[key] = value
                        }
                    }
                } ?: js("undefined")

                analyticsInstance.logEvent(name, jsParams)
                console.log("Analytics event logged: $name", jsParams)
            } ?: run {
                // Fallback to console logging
                val paramsStr = params?.entries?.joinToString(", ") { "${it.key}=${it.value}" } ?: ""
                console.log("Analytics Event (not sent): $name${if (paramsStr.isNotEmpty()) " [$paramsStr]" else ""}")
            }
        } catch (e: Throwable) {
            console.error("Error logging analytics event", e)
        }
    }
}
