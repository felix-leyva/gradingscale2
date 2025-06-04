#!/bin/bash

# Build WasmJS distribution for deployment

echo "Building WasmJS distribution..."
./gradlew :composeApp:wasmJsBrowserDistribution --no-daemon

if [ $? -ne 0 ]; then
    echo "Build failed. Please check your Gradle configuration."
    exit 1
fi

# Distribution output location
DIST_DIR="composeApp/build/dist/wasmJs/productionExecutable"

# Create deployment directory
DEPLOY_DIR="docker/wasmjs-app"
mkdir -p "$DEPLOY_DIR"

# Copy distribution files
echo "Copying distribution files to $DEPLOY_DIR..."
cp -r "$DIST_DIR"/* "$DEPLOY_DIR/"

# Copy Firebase config if it exists
if [ -f "composeApp/src/wasmJsMain/resources/firebase-config.js" ]; then
    echo "Copying Firebase configuration..."
    cp composeApp/src/wasmJsMain/resources/firebase-config.js "$DEPLOY_DIR/"
elif [ -f "composeApp/src/jsMain/resources/firebase-config.js" ]; then
    cp composeApp/src/jsMain/resources/firebase-config.js "$DEPLOY_DIR/"
else
    echo "No firebase-config.js found. Create one from the template if needed."
fi

echo "Build completed successfully!"
echo "Files are ready in $DEPLOY_DIR/"
echo ""
echo "Next steps:"
echo "1. Transfer to server: rsync -avz docker/ user@server:/path/to/gradingscale/"
echo "2. Deploy: docker-compose up -d"