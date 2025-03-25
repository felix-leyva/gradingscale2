package de.felixlf.gradingscale2.network.di

import de.felixlf.gradingscale2.BuildResources
import de.felixlf.gradingscale2.entities.network.BaseUrlProvider

internal class BaseUrlProviderImpl : BaseUrlProvider {
    override val baseApiUrl: String = BuildResources.BASE_URL
}
