# Release Process

This document describes how to create and publish releases for NerdCalci.

## Prerequisites

- Push access to the GitHub repository
- All changes committed and pushed to main branch
- Version bumped in `app/build.gradle.kts`

## Automated Release (Recommended)

### 1. Update Version

Edit `app/build.gradle.kts`:

```kotlin
versionCode = 101  // Increment by 1
versionName = "1.0.1"  // Follow semver
```

**This is the single source of truth!** The workflow extracts the version from here automatically.

### 2. Update Changelogs

Create `fastlane/metadata/android/en-US/changelogs/{versionCode}.txt`:

```bash
# Example for versionCode 101
touch fastlane/metadata/android/en-US/changelogs/101.txt
```

Write changelog (max 500 characters):
```
New features:
• Added feature X
• Improved Y
• Fixed bug Z
```

### 3. Commit with [release] Prefix

```bash
git add .
git commit -m "[release] Release v1.1.0 with new features"
git push origin main
```

**Important:** The commit message MUST start with `[release]` to trigger the release workflow.

### 4. Automated Actions

Once you push the commit, GitHub Actions will automatically:
- Extract version from `build.gradle.kts` (e.g., `1.0.1`)
- Build the release APK
- Create git tag `v1.0.1` automatically
- Create a GitHub release with the APK attached

Check the release at: https://github.com/vishaltelangre/nerdcalci/releases

**No manual tagging required!** The version in gradle is the single source of truth.

## Manual Release

If you need to build locally:

### Build Release APK

```bash
./gradlew assembleRelease
```

The APK will be at: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Create Signed Release (For Play Store)

#### 1. Generate Keystore (First time only)

```bash
keytool -genkey -v -keystore nerdcalci-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias nerdcalci
```

**Important:** Store the keystore file and passwords securely!

#### 2. Configure Signing

Add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../nerdcalci-release-key.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = "nerdcalci"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

#### 3. Build Signed APK

```bash
export KEYSTORE_PASSWORD="your-keystore-password"
export KEY_PASSWORD="your-key-password"
./gradlew assembleRelease
```

## F-Droid Submission

1. Test locally (requires Docker):
   ```bash
   cd /path/to/fdroiddata
   fdroid readmeta
   fdroid rewritemeta com.vishaltelangre.nerdcalci
   fdroid checkupdates --allow-dirty com.vishaltelangre.nerdcalci
   fdroid lint com.vishaltelangre.nerdcalci
   fdroid build com.vishaltelangre.nerdcalci
   ```

2. Commit and push:
   ```bash
   git add metadata/com.vishaltelangre.nerdcalci.yml
   git commit -m "New App: NerdCalci"
   git push origin com.vishaltelangre.nerdcalci
   ```

3. Create a merge request at https://gitlab.com/fdroid/fdroiddata

4. Wait for F-Droid maintainers to review and merge

### Subsequent Updates

F-Droid will automatically detect new releases if:
- You push a new git tag (e.g., `v1.1.0`)
- The tag matches `versionName` in `build.gradle.kts`
- The commit has the corresponding changelog file
