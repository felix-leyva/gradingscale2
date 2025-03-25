package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AnalyticsProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics

internal open class AnalyticsProviderBaseImpl : AnalyticsProvider {
    private val analytics by lazy { Firebase.analytics }

    override fun logEvent(
        name: String,
        params: Map<String, Any>?,
    ) {
        analytics.logEvent(name, params)
    }
}
