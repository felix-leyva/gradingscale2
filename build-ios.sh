#!/bin/bash

# Enable JVM arguments for Kotlin compile
export KOTLIN_DAEMON_JVM_OPTS="-Xmx3G"

# Try to build the Apple framework
cd "$(dirname "$0")"
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode --info

# Check if the build was successful
if [ $? -eq 0 ]; then
    echo "✅ Successfully built Apple framework. Now attempting to build the iOS app."
    cd iosApp
    xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator
else
    echo "❌ Failed to build Apple framework."
fi
