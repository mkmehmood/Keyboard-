# Gboard Keyboard Replica with Unlimited Clipboard 📋⌨️

A replica of **Google Android Keyboard (Gboard)** built with modern Jetpack Compose and Material You design, featuring an **Unlimited Copy-Paste Clipboard Engine** backed by an on-device local Room SQLite database.

---

## 🚀 How to Get the Debug APK from GitHub Releases

This repository includes automated GitHub Actions workflows that automatically compile and publish the debug APK to the **Releases** tab of your GitHub repository.

### Method 1: Push a Git Tag (Recommended)
Pushing any version tag (e.g. `v1.0.0`, `v1.0.1`) triggers the release workflow automatically:

```bash
# Using the helper script:
./scripts/tag-and-release.sh v1.0.0

# Or manually via Git:
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

1. Navigate to your GitHub repository in your browser.
2. Click on the **Releases** tab on the right sidebar.
3. You will see the new release with the attached installable APK:
   - `Gboard-Debug-v1.0.0.apk`
   - `Gboard-Debug.apk`
   - SHA-256 checksum verification files

### Method 2: Manually Trigger from GitHub Actions UI
You can generate a release on demand without creating a tag:

1. Open your repository on GitHub.
2. Go to the **Actions** tab.
3. In the left sidebar, click on **Build and Release Debug APK**.
4. Click the **Run workflow** dropdown button on the right.
5. (Optional) Enter a custom Release Tag (e.g., `v1.0.0`) and Release Title.
6. Click the green **Run workflow** button.
7. Once finished (1-2 minutes), check the **Releases** tab to download the APK.

### Method 3: Download from CI Workflow Run Artifacts
On every push to the `main` or `master` branch:
1. Go to **Actions** ➔ **Android CI Build**.
2. Click on the latest workflow run.
3. Scroll down to **Artifacts** and download `debug-apk.zip`.

### Method 4: Build Locally
To build the debug APK on your computer:

```bash
# Using the helper script:
./scripts/build-apk.sh

# Or directly using Gradle:
./gradlew assembleDebug
```
The resulting APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📲 How to Install & Activate on Android

1. **Download APK**: Download `Gboard-Debug.apk` from the GitHub Releases tab directly onto your Android device.
2. **Install**: Tap the downloaded file. When prompted, allow *Install from Unknown Sources* for your browser or file manager.
3. **Open the App**: Launch **Gboard** from your home screen or app drawer.
4. **Step 1 — Enable Keyboard**:
   - Tap **Open Android Settings** in the app.
   - Toggle **Gboard Keyboard** to ON.
5. **Step 2 — Set as Default**:
   - Tap **Select Gboard Keyboard** and choose Gboard from the switcher.
6. **Step 3 — Test & Enjoy**:
   - Type in the built-in interactive sandbox or open any app (WhatsApp, Messages, Chrome, Notes) to use your new keyboard!

---

## 🌟 Key Features

- **Google Material You QWERTY Layout**: Rounded stadium keycaps, dual-label hierarchy, long-press hint symbols (`q` ➔ `1`, `w` ➔ `2`, `a` ➔ `@`), and visual magnifier pop-up balloons.
- **Unlimited Copy-Paste Engine**: Persistent local Room SQLite database with zero clip expiration. Pinned clips are permanently preserved.
- **Top Suggestion Strip**: Google "G" quick menu, word prediction chips, and 1-tap clipboard paste chip (`📋 Paste: <preview>`).
- **In-Keyboard Clipboard Tray**: Dedicated clipboard drawer with instant search, pin/unpin toggles, category filtering (Code, Email, Link, Phone), and custom template creator.
- **Full Customization**: 7 themes (Gboard Dark, Material You Dynamic, AMOLED Black, Gboard Light, Google Blue, Mint Green, Coral Sunset), dedicated number row toggle, haptic vibration slider, and sound feedback.
- **100% Private & On-Device**: Zero network telemetry. All keystrokes and clipboard history remain strictly inside your device's local database.

---

## 🛠 Project Structure & GitHub Helpers

```
├── .github/
│   └── workflows/
│       ├── release.yml          # Builds & publishes debug APKs to GitHub Releases tab
│       └── build-ci.yml         # CI build & test verification on every commit/PR
├── scripts/
│   ├── build-apk.sh             # Helper to build debug APK locally or in CI
│   └── tag-and-release.sh       # Helper to create git tags and trigger releases
├── app/
│   └── src/main/java/com/example/
│       ├── ime/                 # System InputMethodService & keyboard layouts
│       ├── data/                # Room Database, DAO, & Clipboard Repository
│       └── ui/                  # Companion App & Material You components
├── gradlew                      # Gradle wrapper script (Unix/Linux/macOS)
├── gradlew.bat                  # Gradle wrapper script (Windows)
└── gradle/wrapper/              # Gradle wrapper configuration & JAR
```

## Recent additions
- Settings hub (`settings/SettingsHubActivity`) with deep links into the existing tabs
- Subtypes for English (US/UK), Spanish, French (AZERTY) and German (QWERTZ) with matching letter layouts
- Theme builder (`ThemeBuilderActivity`) for a custom colour theme
- Suggestions, next-word prediction, one-edit autocorrect and double-space period (`ime/engine/SuggestionEngine`)
- Glide typing (`ime/engine/GlideDecoder`, `ime/ui/GlideModifier`)
- User dictionary stored in Room (DB v3, migration keeps clipboard history)

The engine package is plain Kotlin with JVM unit tests in `app/src/test`.
