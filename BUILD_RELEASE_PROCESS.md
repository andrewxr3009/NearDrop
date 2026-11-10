# NearDrop build and release process

This process borrows the Vaibes release discipline: versioned tags, repeatable builds, validation before publication, and a recorded compatibility scope. NearDrop does not inherit Vaibes credentials, signing keys, package IDs, or deployment configuration. This repository is intentionally public, as requested by its owner.

## Local Android build

Requirements: JDK 17+, Android SDK with API 36 and Build Tools 36.0.0, plus the checked-in Gradle wrapper. On the workstation used to start NearDrop, the SDK is under `/media/andrew/599694BD6C63FC621/vaibes-tools/android-sdk/vaibes-android-sdk`; `android/local.properties` contains this machine-specific path and is git-ignored. Update it on other workstations.

```bash
cd android
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The debug APK is `android/app/build/outputs/apk/debug/app-debug.apk`. Debug builds use Android's debug signing key and are for local development only. The app includes NearDrop-to-NearDrop discovery and file transfer through Google Play services Nearby Connections; it does not include AirDrop interoperability.

## Local native validation

Requirements: Rust stable and Cargo. Current `native/Cargo.toml` is a workspace manifest; source and test organization is under `native/crates/` and `native/tests/`.

```bash
cargo fmt --manifest-path native/Cargo.toml --all -- --check
cargo test --manifest-path native/Cargo.toml --workspace
cargo build --manifest-path native/Cargo.toml --workspace
```

Run `cargo test -p neardrop-transport` to check the test-only simulated transport contract. A simulated test result never counts as a nearby-device or AirDrop test.

## GitHub release flow

GitHub Actions runs Rust tests and Android unit tests/debug build for pull requests and pushes to `main`. Pushing a version tag matching `v*` runs the same checks, generates `SHA256SUMS`, and publishes a GitHub prerelease with the debug APK attached. The APK is development-only and is not signed for production distribution.

```bash
git tag v0.1.0
git push origin v0.1.0
```

After the workflow succeeds, review the published prerelease and attached `SHA256SUMS` at GitHub Releases. Confirm the tag and APK hash, test on target hardware, and document the device/OS matrix. Do not title a release “AirDrop compatible” absent two-way tests with unmodified Apple devices and no iPhone companion app.

## Before publishing any stable release

1. Update `versionName`/`versionCode` in `android/app/build.gradle.kts` and release notes.
2. Pass Android unit tests, Rust formatting/tests/build, and Android assembly.
3. Review `git diff --check`, code changes, dependency/license audit and generated APK checksum.
4. Test an installable signed APK with consent and malformed-input cases on actual hardware.
5. Record exact Android model, SoC, Wi-Fi driver/firmware, OS build, peer Apple model/OS and transport for any interoperability result.
6. Publish stable releases only after all claims are supported by repeatable evidence and production signing is configured.

## Signing and repository visibility

No production keystore, passwords, Apple credentials, or certificates are stored in this repository. Production signing must be configured later using protected GitHub Actions secrets or an approved local secret file outside the repository. The GitHub source repository is public by explicit owner request; the root MIT license covers the code, subject to third-party licenses listed in `docs/research/LICENSE_AUDIT.md`.

GitHub CLI authentication is already available in the current environment. Do not paste or commit tokens into this repository.
