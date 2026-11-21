package de.felixlf.gradingscale2.entities.network

/**
 * Analytics provider.
 */
interface AnalyticsProvider {

    /**
     * Logs an event.
     * @param name The name of the event.
     * @param params The parameters of the event.
     */
    fun logEvent(
        name: String,
        params: Map<String, Any>? = null,
    )
}
