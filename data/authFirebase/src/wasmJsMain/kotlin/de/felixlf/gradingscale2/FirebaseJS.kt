package de.felixlf.gradingscale2

// Mock implementations for WasmJS to avoid JS interop issues
class MockFirebaseAuth {
    fun signOut() {
        // No-op for WasmJS
    }
    
    fun getCurrentUser(): String? = null
    
    fun getIdToken(): String? = null
}

class MockFirebaseAnalytics {
    fun logEvent(eventName: String) {
        // No-op for WasmJS - could log to console if needed
    }
}

class MockConsole {
    fun log(message: String) {
        // No-op for WasmJS 
    }
    
    fun warn(message: String) {
        // No-op for WasmJS
    }
    
    fun error(message: String) {
        // No-op for WasmJS
    }
}

// Factory functions that return mocks for WasmJS
fun getFirebaseAuth(): MockFirebaseAuth = MockFirebaseAuth()
fun getFirebaseAnalytics(): MockFirebaseAnalytics = MockFirebaseAnalytics()
fun isFirebaseInitialized(): Boolean = false // Always false for WasmJS
fun getConsole(): MockConsole = MockConsole()
