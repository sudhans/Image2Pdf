# AGENTS.md

## Project Shape

- Single-module Android app: root project `Image2Pdf`, only included module is `:app`.
- Main package is `com.msd.image2pdf`; app entrypoint is
  `app/src/main/java/com/msd/image2pdf/MainActivity.kt`.
- Compose navigation routes are declared inline in `MainActivity`: `main`, `preview`, `pdf_viewer`,
  and `settings`.
- PDF creation logic is concentrated in `PreviewScreen.kt`; persisted settings live in
  `PdfPageOptions.kt` via `SharedPreferences` name `Image2PdfPrefs`.

## Toolchain

- Use the checked-in wrapper: `./gradlew` with Gradle `9.4.1`.
- Android Gradle Plugin is `9.2.1`, Kotlin is `2.2.21`, Java/Kotlin toolchain is `17`.
- App config uses `compileSdk 37`, `targetSdk 37`, and `minSdk 33`; image decoding uses APIs that
  assume this floor.
- Several `gradle.properties` Android flags are deprecated and produce warnings during Gradle
  configuration; these warnings are currently expected.

## Commands

- Build debug APK: `./gradlew :app:assembleDebug`
- Full module build/check: `./gradlew :app:build`
- Lint debug variant: `./gradlew :app:lintDebug`
- Run local unit tests: `./gradlew :app:testDebugUnitTest`
- Run one local test:
  `./gradlew :app:testDebugUnitTest --tests 'com.msd.image2pdf.ExampleUnitTest.addition_isCorrect'`
- Run instrumented tests only with a connected/emulated device:
  `./gradlew :app:connectedDebugAndroidTest`

## Repo-Specific Gotchas

- `app/release/` may contain local release artifacts (`.apk`, `.aab`, expanded bundle files); it is
  not source and should be ignored unless the task is explicitly about release outputs.
- Dependencies are managed in `gradle/libs.versions.toml`; keep plugin/library version edits there,
  not inline in module Gradle files.
- Generated PDFs are written through `MediaStore` to `Downloads/Image2Pdf`, so PDF behavior usually
  needs emulator/device verification rather than plain JVM tests.
- The README badge points to a GitHub Actions workflow, but no `.github/workflows` files are present
  in this checkout; rely on Gradle tasks above for local verification.
