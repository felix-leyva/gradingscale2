package de.felixlf.gradingscale2.entities.network

interface BaseUrlProvider {
    /**
     * The base URL for the API, without the last trailing slash.
     */
    val baseApiUrl: String
}
