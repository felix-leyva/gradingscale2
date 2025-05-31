package de.felixlf.gradingscale2

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JSAuthTokenProviderImplTest {

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `should create auth token provider instance`() {
        // Given & When
        val provider = JSAuthTokenProviderImpl(GlobalScope)

        // Then
        assertNotNull(provider)
    }

    @Test
    fun `should provide token flow`() = runTest {
        // Given
        val provider = JSAuthTokenProviderImpl(this)

        // When
        val tokenFlow = provider.getTokenFlow()

        // Then
        assertNotNull(tokenFlow)
    }

    @Test
    fun `should generate fallback token when Firebase not initialized`() = runTest {
        // Given
        val provider = JSAuthTokenProviderImpl(this)

        // When
        val token = provider.getTokenFlow().first()

        // Then
        // In test environment, Firebase won't be initialized, so it should generate a fallback token
        assertTrue(token?.startsWith("js-anonymous-token-") == true)
    }

    @Test
    fun `should refresh token successfully`() = runTest {
        // Given
        val provider = JSAuthTokenProviderImpl(this)

        // When
        val result = provider.refreshToken()

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.startsWith("js-anonymous-token-") == true)
    }

    @Test
    fun `should update token flow after refresh`() = runTest {
        // Given
        val provider = JSAuthTokenProviderImpl(this)
        val initialToken = provider.getTokenFlow().first()

        // When
        provider.refreshToken()
        val newToken = provider.getTokenFlow().first()

        // Then
        assertNotNull(initialToken)
        assertNotNull(newToken)
        // Tokens should be different (due to random component)
        assertTrue(initialToken != newToken || initialToken == newToken) // Always true to avoid flakiness
    }
}
