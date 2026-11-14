# Tobacco Scanner (Android)

Photograph tobacco (leaf, pile, bale or powder) and get a visual quality report from Claude.
Material 3 UI, built with Jetpack Compose. The app follows the phone's language (English and Urdu included).

## How it works
- You add **your own Anthropic API key** in Settings. It is stored only on the phone and sent only to `api.anthropic.com`.
- Each scan sends one downscaled photo to the Messages API and shows the score, defects, good signs and advice.
- A photo cannot judge smell, moisture, curing method or chemicals. Confirm by smell and touch before buying.

## Build
GitHub Actions builds the APKs (no local setup needed):

| Trigger | Result |
|---|---|
| Push to `main`, pull request, or manual run | Debug and release APKs attached to the workflow run (Actions tab, **Artifacts**) |
| Push a tag such as `v1.0.0` | The same APKs are published on the repository's **Releases** page |

```
git tag v1.0.0 && git push origin v1.0.0
```

Locally (Android SDK 34, JDK 17, Gradle 8.9): `gradle assembleRelease`

## Signing (optional)
Without secrets, the release APK is signed with the debug key so it installs for testing.
For a stable release key, add these repository secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

Create a keystore and its base64 value:
```
keytool -genkeypair -v -keystore release.jks -alias scanner -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 release.jks
```
