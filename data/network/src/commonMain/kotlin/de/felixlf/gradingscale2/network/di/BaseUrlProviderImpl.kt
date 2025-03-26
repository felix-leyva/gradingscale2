package de.felixlf.gradingscale2.network.di

import de.felixlf.gradingscale2.BuildBaseUrls
import de.felixlf.gradingscale2.entities.network.BaseUrlProvider

internal class BaseUrlProviderImpl : BaseUrlProvider {
    override val baseApiUrl: String = BuildBaseUrls.BASE_URL
}
