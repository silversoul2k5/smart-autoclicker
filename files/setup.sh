#!/bin/bash
# Smart AutoClicker - Backup Integration Setup Script
# This script helps integrate the backup auto-loader into your project

set -e

PROJECT_ROOT="${1:-.}"
BACKUP_FILE="${2:SmartAutoClicker-Backup.zip}"

echo "=== Smart AutoClicker Backup Auto-Loader Setup ==="
echo ""
echo "Project Root: $PROJECT_ROOT"
echo "Backup File: $BACKUP_FILE"
echo ""

# Check if this is a gradle project
if [ ! -f "$PROJECT_ROOT/settings.gradle.kts" ] && [ ! -f "$PROJECT_ROOT/settings.gradle" ]; then
    echo "❌ Error: settings.gradle not found. Is this a gradle project?"
    exit 1
fi

echo "✓ Gradle project detected"

# Create assets directory if needed
ASSETS_DIR="$PROJECT_ROOT/app/src/main/assets"
mkdir -p "$ASSETS_DIR"
echo "✓ Assets directory ready: $ASSETS_DIR"

# Copy backup file if it exists
if [ -f "$BACKUP_FILE" ]; then
    cp "$BACKUP_FILE" "$ASSETS_DIR/"
    echo "✓ Backup file copied to assets"
else
    echo "⚠ Backup file not found at: $BACKUP_FILE"
    echo "  Place your backup file manually at: $ASSETS_DIR/SmartAutoClicker-Backup.zip"
fi

# Create DI directory if needed
DI_DIR="$PROJECT_ROOT/core/base/src/main/java/com/buzbuz/smartautoclicker/core/base/di"
mkdir -p "$DI_DIR"
echo "✓ DI directory ready: $DI_DIR"

# Check if BackupAutoLoader.kt already exists
if [ -f "$DI_DIR/BackupAutoLoader.kt" ]; then
    echo "✓ BackupAutoLoader.kt already exists"
else
    echo "⚠ BackupAutoLoader.kt not found"
    echo "  Copy BackupAutoLoader.kt from outputs to: $DI_DIR/"
fi

# Summary
echo ""
echo "=== Setup Summary ==="
echo ""
echo "Next steps:"
echo "1. Copy BackupAutoLoader.kt to: $DI_DIR/"
echo "2. Ensure SmartAutoClicker-Backup.zip is in: $ASSETS_DIR/"
echo "3. Add this to MainActivity.onCreate():"
echo "   BackupAutoLoader.loadBackupIfFirstRun(this)"
echo ""
echo "4. Verify permissions in AndroidManifest.xml:"
echo "   <uses-permission android:name=\"android.permission.READ_EXTERNAL_STORAGE\" />"
echo "   <uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\" />"
echo ""
echo "5. Build and test:"
echo "   ./gradlew assembleDebug"
echo "   adb install -r app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "✓ Setup script completed!"
