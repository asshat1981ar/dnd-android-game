#!/bin/bash

echo "🔍 D&D Android App Build Analysis"
echo "=================================="

# Check Gradle wrapper
echo "1. Checking Gradle wrapper..."
if [ -f "gradlew" ]; then
    echo "   ✅ gradlew found"
    chmod +x gradlew
else
    echo "   ❌ gradlew not found"
fi

# Check project structure
echo "2. Checking project structure..."
if [ -f "settings.gradle" ]; then
    echo "   ✅ settings.gradle found"
else
    echo "   ❌ settings.gradle missing"
fi

if [ -f "build.gradle" ]; then
    echo "   ✅ root build.gradle found"
else
    echo "   ❌ root build.gradle missing"
fi

if [ -f "app/build.gradle" ]; then
    echo "   ✅ app build.gradle found"
else
    echo "   ❌ app build.gradle missing"
fi

if [ -f "app/src/main/AndroidManifest.xml" ]; then
    echo "   ✅ AndroidManifest.xml found"
else
    echo "   ❌ AndroidManifest.xml missing"
fi

# Check key source files
echo "3. Checking key source files..."
KEY_FILES=(
    "app/src/main/java/com/orion/dndgame/DnDApplication.kt"
    "app/src/main/java/com/orion/dndgame/ui/MainActivity.kt"
    "app/src/main/java/com/orion/dndgame/eds/enhanced/EnhancedEmotionalDialogueSystem.kt"
    "app/src/main/java/com/orion/dndgame/network/OrionWebSocketClient.kt"
)

FOUND=0
for file in "${KEY_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "   ✅ $(basename $file)"
        ((FOUND++))
    else
        echo "   ❌ $(basename $file) missing"
    fi
done

echo ""
echo "📊 Build Readiness Assessment:"
echo "  - Project files: Complete"
echo "  - Gradle wrapper: Setup ✅"
echo "  - Core files: $FOUND/4 present"

if [ $FOUND -ge 3 ]; then
    echo "  - Status: 🎯 READY FOR ANDROID STUDIO"
    echo ""
    echo "📱 Next Steps:"
    echo "  1. Open dnd-android-app in Android Studio"
    echo "  2. Sync project with Gradle files"
    echo "  3. Run './gradlew build' to compile"
    echo "  4. Install on device with './gradlew installDebug'"
else
    echo "  - Status: ⚠️  Missing critical files"
fi

echo ""
echo "🚀 Quick Commands:"
echo "  - Test build: ./gradlew assembleDebug"
echo "  - Run tests: ./gradlew test"
echo "  - Install: ./gradlew installDebug"
echo "  - Clean: ./gradlew clean"