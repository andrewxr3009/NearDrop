# ADR-001: Android-first Kotlin UI and Rust protocol core

- **Status:** Accepted
- **Decision:** Kotlin, Jetpack Compose and Material 3 for Android; Rust workspace for portable protocol, session, security and transport code.
- **Reason:** Native Android lifecycle/sharesheet integration pairs with a separately testable systems core. Rust provides strong parsing and memory-safety properties for untrusted wire formats.
- **Consequence:** Two toolchains and build pipelines are required. AWDL remains device/system-dependent. UI remains functional without the native library using the scaffold repository.
