# Fokus StB — Android app

Native Kotlin + Jetpack Compose implementation of the "Fokus Steuerberater" design
(`project/Fokus Steuerberater.dc.html` in the repo root), built for the user's real
study plan (122 Teilthemen, 255 Aufgaben, exam 5.–7. Okt. 2027, from `project/lernplan-data.js`).

## What's implemented

All 6 screens from the prototype, pixel-matched to the Organic design system's tokens
(`project/_ds/.../styles.css`): Heute, Plan, Themen (+ Gebiet detail with notes), Fokus
(real-time focus timer, not the prototype's accelerated demo speed), Beet (progress
garden), and the Profile/Rating/Distraction bottom sheets. All state — task status,
ratings, notes, the Merkliste queue, weekday availability, weight/pace, profile — is
persisted locally via SharedPreferences (one key, mirroring the prototype's single
localStorage key). No accounts, no network calls, nothing leaves the device.

One deliberate deviation from the prototype: "today" is the device's real current date
(`java.time.LocalDate.now()`), not the frozen `2026-08-29` baked into the prototype's
demo data — a shipped app has to keep working correctly on every future day.

## Project layout

Standard single-module Gradle/Android Studio project:

- `app/src/main/java/de/fokusstb/app/data/` — data models, the JSON loader
  (`assets/lernplan.json`, converted from `lernplan-data.js`), and `StateStore`
  (SharedPreferences persistence).
- `app/src/main/java/de/fokusstb/app/presentation/ViewBuilder.kt` — a faithful port of
  the prototype's `renderVals()`: all the date math, coach copy, recall-color scale,
  filtering/sorting, etc., translated into typed view data.
- `app/src/main/java/de/fokusstb/app/AppViewModel.kt` — state transitions (start/pause/
  rate a session, toggle the Merkliste, edit notes, etc.), a port of the prototype's
  `setState` calls.
- `app/src/main/java/de/fokusstb/app/ui/` — Compose screens, shared components
  (progress rings, the growing-plant illustration, pill chips), and theme tokens.

## Building

This container's network policy blocks `dl.google.com`, which is where the Android
SDK's platform files and build-tools (`aapt2`, `d8`) are distributed — so a real `.apk`
could not be assembled inside this session (Maven-hosted dependencies like AndroidX,
Compose, and Kotlin resolved fine from `google()`/`mavenCentral()`; only the SDK
binaries themselves were unreachable). Gradle and a JDK 17 are otherwise wired up and
ready.

To build the APK yourself:

1. Open this folder in Android Studio (Giraffe or newer), or install the Android SDK
   command-line tools + `platforms;android-34` + `build-tools;34.0.0` via `sdkmanager`.
2. `./gradlew assembleDebug` — the debug APK lands at
   `app/build/outputs/apk/debug/app-debug.apk`.
3. Install it: `adb install app/build/outputs/apk/debug/app-debug.apk`, or drag it onto
   an emulator.

No signing config is set up for a release build yet — `assembleDebug` is enough to get
a real, installable APK for testing.
