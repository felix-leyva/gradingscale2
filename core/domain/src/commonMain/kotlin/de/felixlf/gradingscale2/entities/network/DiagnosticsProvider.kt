package de.felixlf.gradingscale2.entities.network

// TODO: integrate logging (errors/warning) with the diagnostics provider
interface DiagnosticsProvider {
    suspend fun initDiagnostics()
}
