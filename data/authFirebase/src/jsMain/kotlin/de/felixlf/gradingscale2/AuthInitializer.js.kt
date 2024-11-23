package de.felixlf.gradingscale2

import dev.gitlive.firebase.externals.initializeApp

internal actual typealias AuthInitializerImpl = JSAuthInitializerImpl

internal class JSAuthInitializerImpl : AuthInitializer {
    override operator fun invoke() {
        initializeApp(options = {
        })
    }
}
