<p align="center">
  <img src="https://github.com/Nain57/SmartAutoClicker/blob/master/smartautoclicker/src/main/ic_smart_auto_clicker-playstore.png?raw=true" height="64">
  <h3 align="center">Klick'r - Smart AutoClicker</h3>
  <p align="center">An Autoclicker Based On Image Detection</p>
</p>

<p>Klick'r is an open-source Android automation app that combines image-based detection with traditional auto-clicking. It is built as a modular Gradle Kotlin DSL Android project and can automate repetitive taps, swipes, and custom event flows on Android devices.</p>

## Table of Contents
- [How It Works](#how-it-works)
- [Main Features](#main-features)
- [Project Structure](#project-structure)
- [Build & Run](#build--run)
- [Usage](#usage)
- [Contributing](#contributing)
- [GitHub Upload](#github-upload)
- [Links](#links)

## How It Works
Klick'r works in two complementary ways:

1. **Image Detection Automation**
   - The app can detect UI elements by matching image templates against the current screen.
   - When an image is found, Klick'r can perform taps, swipes, and follow-up actions automatically.
   - This makes it useful for automating apps or games where control buttons are not always at fixed coordinates.

2. **Regular Auto-Click Mode**
   - For simpler use cases, Klick'r supports basic auto-clicking at fixed screen positions.
   - You can configure click intervals, touch duration, and repeat count.

3. **Triggers and Actions**
   - Automation can be driven by:
     - image detection triggers
     - timer events
     - counter thresholds
     - Android intents or broadcasts
   - Actions include taps, swipes, delays, counter updates, and advanced flow control.

4. **Android Architecture**
   - The repository is a multi-module Android project with shared `core` modules and feature modules.
   - The Android app module is `Smart-AutoClicker/smartautoclicker`.
   - The app uses Kotlin, Gradle, and AndroidX libraries.

## Main Features
- Image-based detection to trigger clicks precisely when UI elements appear.
- Configurable click and swipe automation.
- Regular click mode for repeated tap patterns.
- Advanced automation flows with counters and delayed actions.
- Support for Android broadcast receivers and intent-based triggers.
- Tutorials to help new users learn the automation workflow.
- Modular source code organized for reuse and extension.

## Project Structure
- `Smart-AutoClicker/smartautoclicker/` — Android application module.
- `core/` — shared libraries used by the app and feature modules.
- `feature/` — feature-specific Gradle modules such as smart-config, quick-settings-tile, notifications, etc.
- `gradle/` and `build-logic/` — build configuration and shared Gradle plugin logic.
- `documentation/` — diagrams and supporting documentation.

## Build & Run
### Prerequisites
- Java JDK 11+ installed.
- Android SDK installed and configured.
- `adb` available if you want to install the app on a device.

### Build the App
From the repository root:

```bash
./gradlew clean assembleDebug
```

### Install on a connected device

```bash
./gradlew :Smart-AutoClicker:installDebug
```

### Open in Android Studio
1. Open the repository root in Android Studio.
2. Sync Gradle.
3. Run the `Smart-AutoClicker` app on an emulator or connected Android device.

## Usage
1. Open the app on your Android device.
2. Choose between image-based automation or Regular Mode.
3. Configure the target image template and action sequence.
4. Start the automation flow.
5. Monitor the execution and adjust settings as needed.

> Note: Android accessibility permissions or screen capture permissions may be required for automation features to work.

### Example Use Case: Claiming Red Packets (e.g., in messaging apps)

Klick'r can be configured to automatically claim "red packets" or similar timed rewards in messaging apps by using image-detection triggers and a small action sequence. Typical steps:

- Capture a clear template image of the red packet button or notification bubble.
- Create an image-detection trigger that watches for the template to appear on-screen.
- Configure the action sequence to:
   - Tap the detected red packet location.
   - Wait for the opening animation (configurable delay).
   - Tap the confirmation/collect button (if needed) using a second image template or a fixed coordinate.
   - Optionally increment counters or log the claim event.

Security & Ethics: Use automation responsibly. Automating interactions in third-party apps may violate their terms of service. Only automate actions on accounts and apps where you have permission and where doing so is legal and ethical.

## Contributing
- Use branches for features and fixes.
- Add clear commit messages.
- Open issues for bugs or enhancement requests.
- If you change build logic, keep the `core` and `feature` module boundaries clear.

## GitHub Upload
To upload this repository to `https://github.com/silversoul2k5/smart-autoclicker`, run:

```bash
cd /home/arjun/Projects/krlicker/Smart-AutoClicker
git remote set-url origin https://github.com/silversoul2k5/smart-autoclicker.git
git push -u origin master
```

If the target repo is empty and you want to force the current branch:

```bash
git push -u origin master --force
```

## Links
- Wiki: https://github.com/Nain57/Smart-AutoClicker/wiki
- Bug report: https://github.com/Nain57/Smart-AutoClicker/issues/new?template=bug_report.yml
- Obfuscated release: https://github.com/Nain57/Smart-AutoClicker/releases/tag/obfuscation-start

