package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AnalyticsProvider

class JSAnalyticsProviderImpl : AnalyticsProvider {
    private val analytics = getFirebaseAnalytics()
    private val console = getConsole()

    override fun logEvent(
        name: String,
        params: Map<String, Any>?,
    ) {
        // For WasmJS, use mock implementation to avoid runtime errors
        analytics.logEvent(name)
        
        // Mock console logging (no-op)
        val paramsStr = params?.entries?.joinToString(", ") { "${it.key}=${it.value}" } ?: ""
        console.log("Analytics Event: $name${if (paramsStr.isNotEmpty()) " [$paramsStr]" else ""}")
    }
}
