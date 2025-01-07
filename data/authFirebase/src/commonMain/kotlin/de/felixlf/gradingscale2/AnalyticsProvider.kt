package de.felixlf.gradingscale2

interface AnalyticsProvider {
    fun logEvent(
        name: String,
        params: Map<String, Any>? = null,
    )
}
