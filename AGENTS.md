# AGENTS.md

This document defines standard expectations for coding agents working in this repository.

## Scope

These rules apply to the entire repository unless a deeper `AGENTS.md` overrides them.

## Project Snapshot

- App: `NerdCalci` (Android calculator for power users)
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Persistence: Room
- Build: Gradle (`./gradlew`)
- Min SDK: 23

## Core Workflow

1. Understand the requested change before editing.
2. Make the smallest clear diff that solves the problem.
3. Preserve existing behavior unless the task explicitly changes it.
4. Run the narrowest relevant verification command(s).
5. Report what changed, where, and how it was validated.

## Editing Rules

- Do not revert unrelated local changes.
- Keep code readable and consistent with surrounding style.
- Prefer existing architecture and patterns over introducing new abstractions.
- Prefer updating existing files/components before adding new ones.
- Keep comments minimal and only for non-obvious logic.

## UI/UX Standards (Compose)

- Use Material 3 components and `MaterialTheme` tokens for color/typography/shape.
- Prefer theme-driven styling over hard-coded visual values where practical.
- Keep visual hierarchy clear: primary action, secondary action, tertiary action.
- Empty states should be helpful and actionable (clear message + explicit CTAs).
- Prefer concise copy; avoid instructional walls of text.

## Data and Domain Safety

- Keep Room entity/schema changes backward compatible unless migration is included.
- If database behavior changes, update/add migration and tests.
- Respect existing limits/constants (for example max file-name length, pinned file cap).

## Verification Commands

Run what is relevant to the change:

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

- For UI-only/refactor changes, compile is minimum.
- For logic/math/data changes, run unit tests.

## Release and Changelog

- Version source of truth is `app/build.gradle.kts` (`versionCode`, `versionName`).
- For release work, add/update:
  - `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`
- Keep changelog entries concise and user-facing.

## Important Paths

- App code: `app/src/main/java/com/vishaltelangre/nerdcalci`
- Unit tests: `app/src/test/java/com/vishaltelangre/nerdcalci`
- Android tests: `app/src/androidTest/java/com/vishaltelangre/nerdcalci`
- Fastlane metadata: `fastlane/metadata/android/en-US`
- Release guide: `RELEASE.md`

## Caution

- Do not change signing/release secrets handling unless explicitly requested.
- Do not perform destructive git operations.
- Do not add broad dependencies unless required for the task.
