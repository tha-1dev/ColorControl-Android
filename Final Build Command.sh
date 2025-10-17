#!/bin/bash
# build-final-apk.sh

echo "🔍 Checking for potential issues..."
./gradlew lintDebug

echo "🧹 Cleaning project..."
./gradlew clean

echo "🔨 Building Debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Debug build successful!"
else
    echo "❌ Debug build failed. Checking errors..."
    ./gradlew build --stacktrace
    exit 1
fi

echo "📦 Building Release APK..."
./gradlew assembleRelease

if [ $? -eq 0 ]; then
    echo "🎉 Release build successful!"
    echo ""
    echo "📱 APK Locations:"
    echo "   Debug: app/build/outputs/apk/debug/app-debug.apk"
    echo "   Release: app/build/outputs/apk/release/app-release.apk"
else
    echo "❌ Release build failed."
    exit 1
fi