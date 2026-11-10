# NearDrop

NearDrop is an Android-first proximity sharing project. Its long-term research goal is real AirDrop interoperability, while its initial app foundation provides a native Android sharing UI and explicit test transport. **AirDrop compatibility is not implemented or claimed.** No fake Apple peers or discovery results are generated.

## Project status

- Android app: Compose UI, Android Sharesheet target for `ACTION_SEND`/`ACTION_SEND_MULTIPLE`, document picker, and real NearDrop-to-NearDrop discovery and file transfer using Google Play services Nearby Connections.
- Receiving: user-controlled visibility for up to 10 minutes, matching-code confirmation on both devices, and separate accept/reject decision for each file offer. Completed files are copied into `Downloads/NearDrop` on Android 10+.
- Quick Share remains reachable through Android's Sharesheet. It is owned by the system and is not the NearDrop transport.
- Rust: modular protocol/domain/transport/security crates and JNI boundary scaffold; Android Gradle does not yet build or package the native library.
- AWDL, AirDrop wire protocol, Apple identity/contact proofs, and Apple device discovery: not implemented. Nearby Connections is a distinct Android transport and does not interoperate with AirDrop or Quick Share's private transfer protocol.
- `SimulatedTransport` exists for development only; it never appears as a nearby real device.

See [technical research](docs/research/EXISTING_IMPLEMENTATIONS.md), [license audit](docs/research/LICENSE_AUDIT.md), [architecture](docs/architecture/ARCHITECTURE.md), and [hardware matrix](docs/compatibility/HARDWARE_MATRIX.md).

## Releases

GitHub version tags run the Rust and Android checks, then publish a prerelease with the debug APK and SHA-256 checksum. The APK is development-only and uses the Android debug key; it is not production signed. See [BUILD_RELEASE_PROCESS.md](BUILD_RELEASE_PROCESS.md).

## Open and run

Open `android/` in Android Studio with JDK 17 or newer and an installed Android SDK. Select the `app` configuration and run on an emulator or device. Gradle Kotlin DSL and wrapper are included. `android/local.properties` points at the SDK installed on this workstation and is git-ignored; update it on other machines.

Use Android's system Sharesheet to send content from another app into NearDrop, or choose files with the in-app document picker. The Send screen discovers other NearDrop installs while open; both devices need Google Play services, Nearby permissions, Bluetooth/Wi-Fi enabled, and compatible NearDrop builds. Choose a peer to request a connection, compare the authentication code on both screens, and confirm on both devices. The receiver then reviews file names and sizes and explicitly accepts or rejects the offer. Received files are saved under `Downloads/NearDrop` (Android 10+).

The Receive screen advertises for at most ten minutes after the user enables visibility. This build has no foreground service, so Android may stop discovery if it kills the app process. Quick Share can also be selected from Android's Sharesheet, where the system owns its own discovery and transfer. Neither path makes NearDrop compatible with AirDrop.

Build the native workspace where Rust is installed:

```sh
cargo test --workspace --manifest-path native/Cargo.toml
cargo build --workspace --manifest-path native/Cargo.toml
```

Build JNI libraries for Android only after installing the Rust Android targets and `cargo-ndk`; these are not installed automatically by this repository.

## Modules

`android/app` is the app shell, permissions, and intent integration. `android/core` owns shared models and native boundary; `design-system` owns reusable Compose surfaces; `feature-*` packages own UI areas; `data` owns the Nearby Connections transport and explicitly marked simulated transport. `native/crates` splits domain, wire formats, discovery, transport, security and JNI concerns.

## Security and compatibility

Incoming transfers require explicit connection confirmation and a separate visible accept/reject decision. The Nearby path limits offers to 50 files, 2 GiB per file and 5 GiB total, sanitizes displayed names, and checks declared versus actual size before publishing. It does not accept archives automatically. Nearby Connections is a distinct NearDrop-to-NearDrop transport; a LAN or Nearby transfer does not implement AirDrop. AWDL needs device/OS/vendor integration unavailable to ordinary Android apps; consult the hardware matrix before making device claims.

## License

The NearDrop starter code is MIT-licensed. Third-party research and projects are references only unless explicitly added with a separate dependency and license review. See `docs/research/LICENSE_AUDIT.md`.
