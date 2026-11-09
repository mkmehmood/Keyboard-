#!/usr/bin/env bash
set -e

# ==============================================================================
# Helper Script: Build Debug APK for Gboard Replica
# ==============================================================================

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "==> Preparing build environment in $PROJECT_ROOT"

# Ensure .env exists
if [ ! -f ".env" ]; then
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo "--> Created .env from .env.example"
    else
        touch .env
        echo "--> Created empty .env"
    fi
fi

# Ensure debug.keystore exists
if [ ! -f "debug.keystore" ]; then
    if [ -f "debug.keystore.base64" ]; then
        echo "--> Decoding debug.keystore from base64..."
        base64 -d debug.keystore.base64 > debug.keystore
    else
        echo "--> Generating fresh debug.keystore..."
        keytool -genkey -v \
            -keystore debug.keystore \
            -storepass android \
            -alias androiddebugkey \
            -keypass android \
            -keyalg RSA \
            -keysize 2048 \
            -validity 10000 \
            -dname "CN=Android Debug,O=Android,C=US"
    fi
fi

# Determine gradle command
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    GRADLE_CMD="./gradlew"
elif command -v gradle &> /dev/null; then
    GRADLE_CMD="gradle"
else
    echo "Error: Neither ./gradlew nor gradle command found."
    exit 1
fi

echo "==> Running unit tests..."
$GRADLE_CMD :app:testDebugUnitTest

echo "==> Building Debug APK..."
$GRADLE_CMD :app:assembleDebug

APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo "========================================================================"
    echo " SUCCESS: Debug APK built successfully!"
    echo " Location: $APK_PATH"
    echo " Size: $(du -h "$APK_PATH" | cut -f1)"
    echo "========================================================================"
else
    echo "Error: Build succeeded but APK was not found at $APK_PATH"
    exit 1
fi
