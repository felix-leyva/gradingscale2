#!/bin/bash

# Stop any running Kotlin daemons
./gradlew --stop

# Clean build directories
rm -rf .gradle
rm -rf .kotlin
rm -rf build
rm -rf composeApp/build
rm -rf iosApp/build
rm -rf kotlin-js-store

# Rebuild the project
./gradlew clean
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
