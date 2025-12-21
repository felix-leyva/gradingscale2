plugins {
    id("io.sentry.android.gradle")
}
sentry {
    // Prevent Sentry dependencies from being included in the Android app through the AGP.
    autoInstallation {
        enabled.set(false)
    }

    org.set(System.getenv("SENTRY_ORG") ?: throw NullPointerException("SENTRY_ORG environmental variable not set yet"))
    projectName.set(
        System.getenv("SENTRY_PROJECT_NAME") ?: throw NullPointerException("SENTRY_PROJECT_NAME environmental variable not set yet"),
    )
    authToken.set(System.getenv("SENTRY_AUTH_TOKEN") ?: throw NullPointerException("SENTRY_AUTH_TOKEN environmental variable not set yet"))
}
